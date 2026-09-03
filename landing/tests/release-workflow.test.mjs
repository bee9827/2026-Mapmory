import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import { spawnSync } from "node:child_process";
import test from "node:test";

const workflow = readFileSync(new URL("../../.github/workflows/landing-release.yml", import.meta.url), "utf8");
const script = readFileSync(new URL("../scripts/deploy-ec2.sh", import.meta.url), "utf8").replace(/\r\n/g, "\n");
const bash = process.platform === "win32" ? "C:/Program Files/Git/bin/bash.exe" : "/bin/bash";

test("release validation runs for every release PR with the ruleset's unique check name", () => {
  assert.match(workflow, /pull_request:\s+branches: \[landing-release\]/);
  assert.match(workflow, /push:\s+branches: \[landing-release\]/);
  assert.doesNotMatch(workflow, /paths(?:-ignore)?:/);
  assert.match(workflow, /name: Landing release validation/);
  assert.ok(workflow.indexOf("run: npm run build") < workflow.indexOf("run: npm test"));
});

test("production deploy is gated by repository, release branch, tests, and environment", () => {
  const deploy = workflow.split("  deploy:\n")[1];
  assert.ok(deploy);
  assert.match(deploy, /github\.repository == 'woowacourse-teams\/2026-Mapmory'/);
  assert.match(deploy, /github\.event_name != 'pull_request'/);
  assert.match(deploy, /github\.ref == 'refs\/heads\/landing-release'/);
  assert.match(deploy, /needs: validate/);
  assert.match(deploy, /environment:\s+name: landing-production/);
  assert.match(deploy, /cancel-in-progress: false/);
  assert.match(deploy, /head_sha.*!=.*GITHUB_SHA/);
});

test("deployment packages static assets and checks the public commit identity", () => {
  assert.match(workflow, /tar -czf.*-C dist\/client \./);
  assert.match(workflow, /GITHUB_SHA.*> dist\/client\/release\.txt/);
  assert.match(workflow, /deployed_sha.*==.*GITHUB_SHA/);
  assert.match(workflow, /StrictHostKeyChecking=yes/);
  assert.doesNotMatch(workflow, /secrets\.EC2_|backend-release|pull_request_target/);
});

test("deployment shell syntax is valid", { skip: !existsSync(bash) }, () => {
  const result = spawnSync(bash, ["-n"], { input: script, encoding: "utf8" });
  assert.equal(result.status, 0, result.stderr || String(result.error));
});

test("deployment rejects invalid paths before running remote mutations", { skip: !existsSync(bash) }, () => {
  const sha = "a".repeat(40);
  for (const release of [sha + "-42", sha + "-12345-2"]) {
    const result = spawnSync(bash, ["-s", "--", release, "/tmp/not-a-landing-archive.tar.gz"], {
      input: script, encoding: "utf8",
    });
    assert.equal(result.status, 2, result.stderr || String(result.error));
    assert.match(result.stderr, /Invalid archive path/);
  }
  for (const release of ["../outside", sha + "-12;echo unsafe", sha + "-12-3-4"]) {
    const result = spawnSync(bash, ["-s", "--", release, "/tmp/mapmory-landing-12345-2.tar.gz"], {
      input: script, encoding: "utf8",
    });
    assert.equal(result.status, 2, result.stderr || String(result.error));
    assert.match(result.stderr, /Invalid release id/);
  }
});
