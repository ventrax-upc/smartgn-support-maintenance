"""Local acceptance against real IAM/Installation/Billing. Purchase and delivery are explicit fixtures."""
from urllib.request import Request,urlopen
from urllib.error import HTTPError,URLError
from pathlib import Path
from datetime import datetime,timezone,timedelta
import json,uuid,secrets,time,sys
settings=dict(x.split('=',1) for x in Path(sys.argv[1] if len(sys.argv)>1 else '../smartgn-payments-billing/.env').read_text().splitlines() if '=' in x and not x.startswith('#'))
SUP='http://localhost:8082';IAM='http://localhost:18080';INST='http://localhost:18081';DEL='http://localhost:18091'
service={'Authorization':'Bearer '+settings['INTERNAL_SERVICE_TOKEN']}
checks=[]
def call(method,path,body=None,auth=None,base=SUP,expected=200):
 req=Request(base+path,data=None if body is None else json.dumps(body).encode(),method=method,headers={'Content-Type':'application/json',**(auth or {})})
 try:
  with urlopen(req,timeout=15) as r:status=r.status;raw=r.read()
 except HTTPError as e:status=e.code;raw=e.read()
 assert status==expected,f'{method} {path}: {status} != {expected}: {raw[:240]}'
 return json.loads(raw) if raw else None
def check(label):checks.append(label);print('PASS',label,flush=True)
def eventually(fn):
 for _ in range(90):
  try:
   value=fn()
   if value:return value
  except (AssertionError,URLError,ConnectionError):pass
  time.sleep(1)
 raise AssertionError('Dependency did not become ready')
def login(email,password):
 r=call('POST','/api/v1/auth/sign-in',{'email':email,'password':password},base=IAM)
 return {'Authorization':'Bearer '+r.get('token',r.get('accessToken',''))}
def activate(email,password):
 rows=eventually(lambda:call('GET','/fixtures/messages?email='+email,auth={'Authorization':'Bearer '+settings['FIXTURE_ADMIN_TOKEN']},base=DEL))
 token=next(r['token'] for r in reversed(rows) if r.get('purpose')=='ACTIVATION')
 call('POST','/api/v1/auth/activate-password',{'activationToken':token,'newPassword':password},base=IAM,expected=204)
 return login(email,password)
for base in (SUP,IAM,INST):eventually(lambda b=base:call('GET','/actuator/health',base=b).get('status')=='UP')
check('Local services healthy')
call('GET','/api/v1/support/tickets',expected=401);call('GET','/api/v1/support/tickets',auth={'Authorization':'Bearer invalid'},expected=401)
check('Anonymous and invalid credentials rejected')
email='support-'+uuid.uuid4().hex+'@example.test';password='Support-'+secrets.token_hex(12)
account=call('POST','/internal/v1/accounts/purchase-confirmations',{'eventId':str(uuid.uuid4()),'purchaseOrderId':str(uuid.uuid4()),'email':email,'firstName':'Support','lastName':'Verification','confirmed':True},service,base=IAM)['accountId']
owner=activate(email,password)
import base64,hmac,hashlib
encode=lambda raw:base64.urlsafe_b64encode(raw).rstrip(b'=').decode()
claims={'iss':'smartgn-iam-service','aud':['smartgn-api'],'sub':account,'accountId':account,'role':'USER','accountKind':'PRINCIPAL','sessionVersion':0,'iat':int(time.time())-120,'exp':int(time.time())-60}
head=encode(json.dumps({'alg':'HS256'}).encode());body=encode(json.dumps(claims).encode());signed=head+'.'+body;token=signed+'.'+encode(hmac.new(settings['JWT_SECRET'].encode(),signed.encode(),hashlib.sha256).digest())
call('GET','/api/v1/support/tickets',auth={'Authorization':'Bearer '+token},expected=401)
check('Expired signed JWT rejected by current IAM validation')
admin=login('admin@smartgn.test',settings['BOOTSTRAP_SUPERADMIN_PASSWORD'])
prop=call('POST','/api/v1/properties',{'name':'Support verification','address':'Lima local test','propertyType':'HOUSE'},owner,base=INST,expected=201)
meter=call('POST','/api/v1/properties/'+prop['id']+'/supply-points',{'serialNumber':'support-'+uuid.uuid4().hex,'locationName':'Kitchen'},owner,base=INST,expected=201)['id']
root='/api/v1/support'
call('POST',root+'/technicians',{'fullName':'Forbidden','accreditationCode':'no','email':'no@example.test','phone':'999'},owner,expected=403)
tech=call('POST',root+'/technicians',{'fullName':'Support Technician','accreditationCode':uuid.uuid4().hex,'specialty':'Gas','email':'tech@example.test','phone':'999999999'},admin,expected=201)
t=call('POST',root+'/tickets',{'meterId':meter,'subject':'Written problem','description':'Local verification'},owner,expected=201);tid=t['ticketId']
call('POST',root+'/tickets/'+tid+'/replies',{'body':'Forged','authorAccountId':str(uuid.uuid4())},owner,expected=403)
# Real PNG multipart upload, private download and invalid photo rejection.
import struct,zlib
chunk=lambda name,data:struct.pack('!I',len(data))+name+data+struct.pack('!I',zlib.crc32(name+data)&0xffffffff)
png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',1,1,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(b'\0\0\0\0'))+chunk(b'IEND',b'')
boundary='support-'+uuid.uuid4().hex
multipart=('--'+boundary+'\r\nContent-Disposition: form-data; name="file"; filename="photo.png"\r\nContent-Type: image/png\r\n\r\n').encode()+png+('\r\n--'+boundary+'--\r\n').encode()
request=Request(SUP+root+'/tickets/'+tid+'/photos',data=multipart,headers={**owner,'Content-Type':'multipart/form-data; boundary='+boundary},method='POST')
with urlopen(request) as response:photo=json.load(response)
with urlopen(Request(SUP+root+'/tickets/'+tid+'/photos/'+photo['photoId']+'/download',headers=owner)) as response:assert response.read()==png
check('Real photo upload and authorized download')
call('POST',root+'/tickets/'+tid+'/replies',{'body':'Resolved remotely' ,'resolve':True},admin,expected=201)
call('POST',root+'/tickets/'+tid+'/close',auth=owner)
call('POST',root+'/tickets/'+tid+'/rating',{'score':5,'comment':'Local verification'},owner,expected=201)
call('POST',root+'/tickets/'+tid+'/rating',{'score':5},owner,expected=409)
check('Written resolution, owner closure, unique rating and forged author rejection')
call('GET',root+'/tickets?size=1',auth=owner)
call('GET',root+'/tickets?page=-1',auth=owner,expected=400)
check('Scoped collection and pagination validation')
t=call('POST',root+'/tickets',{'meterId':meter,'subject':'Visit problem','description':'On-site verification'},owner,expected=201);tid=t['ticketId']
delegate_email='delegate-'+uuid.uuid4().hex+'@example.test'
delegate=call('POST','/api/v1/subusers',{'email':delegate_email,'firstName':'Delegate','lastName':'Verification'},owner,base=IAM,expected=201)['accountId']
delegated=activate(delegate_email,password)
call('PUT','/api/v1/subusers/'+delegate+'/grants',{'meterId':meter,'permissions':['USE_VISITS']},owner,base=IAM,expected=204)
call('GET',root+'/tickets/'+tid,auth=delegated,expected=403)
call('GET',root+'/tickets/'+tid+'/visits',auth=delegated)
call('PUT','/api/v1/subusers/'+delegate+'/grants',{'meterId':meter,'permissions':[]},owner,base=IAM,expected=204)
call('GET',root+'/tickets/'+tid+'/visits',auth=delegated,expected=403)
check('Independent visit grant and immediate revocation with same JWT')
start=datetime.now(timezone.utc)+timedelta(hours=1);end=start+timedelta(hours=2)
window={'requestedWindowStart':start.isoformat(),'requestedWindowEnd':end.isoformat()}
v=call('POST',root+'/tickets/'+tid+'/visits',window,owner,expected=201);vid=v['visitId']
call('POST',root+'/visits/'+vid+'/complete',{'performedWork':'Work','diagnosis':'Too early','finalCost':20},admin,expected=409)
call('POST',root+'/visits/'+vid+'/schedule',window,admin)
call('POST',root+'/visits/'+vid+'/start',auth=admin)
from concurrent.futures import ThreadPoolExecutor
def attempt(path,body,auth):
 req=Request(SUP+path,data=json.dumps(body).encode(),headers={**auth,'Content-Type':'application/json'},method='POST')
 try:
  with urlopen(req,timeout=20) as response:return response.status
 except HTTPError as error:return error.code
with ThreadPoolExecutor(max_workers=2) as pool:statuses=list(pool.map(lambda _:attempt(root+'/visits/'+vid+'/complete',{'performedWork':'Inspection','diagnosis':'Completed locally','finalCost':20},admin),range(2)))
assert sorted(statuses)==[201,409],statuses
check('Concurrent completion commits one diagnosis and one event')
call('POST',root+'/tickets/'+tid+'/close',auth=owner)
check('Assigned, scheduled, started, completed and closed visit without advance payment')
with ThreadPoolExecutor(max_workers=2) as pool:statuses=list(pool.map(lambda _:attempt(root+'/tickets/'+tid+'/rating',{'score':5},owner),range(2)))
assert sorted(statuses)==[201,409],statuses
check('Concurrent rating creates one result')
statement=eventually(lambda:call('GET','/api/v1/technical-statements/'+vid,auth=owner,base='http://localhost:28083'))
assert float(statement['cost'])==20
check('Durable HTTP completion reaches real Billing')
eventually(lambda:call('GET',root+'/visits/'+vid+'/diagnosis',auth=owner).get('billingReferenceId')==statement['order_id'])
check('Automatic durable CoverageCalculated HTTP return without MQTT')
event={'event_id':str(uuid.uuid4()),'event_type':'CoverageCalculated' ,'aggregate_id':vid,'payload':{'visitId':vid,'accountId':account,'cost':statement['cost'],'balance':statement['balance'],'covered':statement['covered'],'orderId':statement['order_id']}}
call('POST','/internal/v1/events',event,service)
call('POST','/internal/v1/events',event,service)
call('POST','/internal/v1/events',{**event,'payload':{**event['payload'],'cost':999}},service,expected=409)
diagnosis=call('GET',root+'/visits/'+vid+'/diagnosis',auth=owner)
assert diagnosis['billingReferenceId']==statement['order_id']
check('Actual Billing reference, idempotent receipt and conflicting event rejection')
foreign_email='foreign-'+uuid.uuid4().hex+'@example.test'
call('POST','/internal/v1/accounts/purchase-confirmations',{'eventId':str(uuid.uuid4()),'purchaseOrderId':str(uuid.uuid4()),'email':foreign_email,'firstName':'Foreign','lastName':'Verification','confirmed':True},service,base=IAM)
foreign=activate(foreign_email,password)
assert call('GET',root+'/tickets',auth=foreign)==[]
call('GET',root+'/tickets/'+tid,auth=foreign,expected=403)
check('Independent principal cannot list or access another account tickets')
call('POST','/api/v1/auth/change-password',{'currentPassword':password,'newPassword':password+'x'},owner,base=IAM,expected=204)
call('GET',root+'/tickets',auth=owner,expected=401)
check('Session revocation rejects formerly valid JWT')
print('Scenarios passed:',len(checks))
