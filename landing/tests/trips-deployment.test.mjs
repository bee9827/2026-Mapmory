import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const read=path=>readFileSync(new URL(path,import.meta.url),'utf8');
test('Trips is built and tested in every combined release path',()=>{
  for(const file of ['../buildspec.yml','../../.github/workflows/landing-release.yml']){
    const text=read(file);
    for(const command of ['ci','run build','test']) assert.ok(text.includes('npm --prefix trips '+command),file+': '+command);
    assert.ok(text.indexOf('npm --prefix trips test')<text.indexOf('bash scripts/package-codedeploy.sh'));
  }
  const main=read('../../.github/workflows/landing-cicd.yml');
  for(const command of ['ci','run build','test']) assert.ok(main.includes('npm --prefix ../trips '+command));
  const packager=read('../scripts/package-codedeploy.sh');
  assert.match(packager,/trips\/dist\/trips/);
  assert.match(packager,/client\/trips\/release\.txt/);
});
test('Nginx example redirects with query preservation and no root fallback',()=>{
  const config=read('../nginx/trips.conf');
  assert.match(config,/location = \/trips\s*\{\s*return 308 \/trips\/\$is_args\$args;/);
  assert.match(config,/location = \/trips\//);
  assert.match(config,/try_files \$uri =404;/);
  assert.doesNotMatch(config,/proxy_pass|listen |server_name|ssl_certificate/);
  // Server configuration is operator-managed, not applied by the release hook.
  assert.doesNotMatch(read('../codedeploy/activate.sh'),/nginx\/trips\.conf|\/etc\/nginx/);
});
test('deployment validates Trips identity, shell and canonical redirect',()=>{
  const hook=read('../codedeploy/activate.sh');
  assert.match(hook,/trips_sha.*==.*sha/);
  assert.match(hook,/trips_html.*photo-input/);
  assert.ok(hook.includes('308 https://map-mory.com/trips/?deployment=$deployment_id'));
});
