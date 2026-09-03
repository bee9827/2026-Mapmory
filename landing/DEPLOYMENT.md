# Landing release — institution-managed AWS pipeline

## Shared-account safety (mandatory)

- Before creating or changing any AWS resource, announce the exact targets, sequence, expected cost and operational risks to the user. Report what actually changed afterward. Read-only inspection must stay within Mapmory resources.
- Reuse the institution account and its designated roles. Do not create IAM users, access keys or OIDC roles, modify shared policies, widen SSH/network access, or inspect other teams' resources.
- Every created resource must carry `Service=techcourse`, `Role=techcourse-etc`, `ProjectTeam=Mapmory`. Apply the common tags in `codedeploy/aws-resources.json` to **each** application, deployment group, build project and pipeline; the JSON is a blueprint, not a CLI request.
- Monthly team limits provided by the institution: August $50, September $60, October onward $70. EC2 estimates are not the complete team bill. Check the team's usage before provisioning; CodeBuild minutes, CodePipeline and S3 add cost. Do not create budgets, dashboards or alarms with extra costs without discussion.
- Use `techcourse-project-2026` for frontend source/build/deployment artifacts. CodePipeline creates the Mapmory-identifiable `mapmory-landing-release/` artifact prefix automatically. Do not change bucket policy, lifecycle or other prefixes. Do not use the backend artifact bucket for frontend serving/deployment.
- Permissions or bucket-role mismatches must be raised in the institution's technical-review channel. Never work around them by changing shared IAM policies. Most deletion permissions are absent; avoid speculative resources.

## Branch, review and approval

Canonical repository is `woowacourse-teams/2026-Mapmory` (`upstream`). `landing-release` is the landing-only production branch. Its initial SHA is `e91d82c727878da8ab05bee1f330f348b3542df3`, with the same landing tree as merged/reviewed PR #240.

All updates go through a PR to `landing-release`, CodeRabbit review and all required/path-relevant checks. Rate-limited or skipped bot reviews are not completed reviews. Use a merge commit and retain the source branch. Never push directly, force-push or delete the release branch. The existing [Landing release ruleset](https://github.com/woowacourse-teams/2026-Mapmory/rules/22160463) requires `Landing release validation`, resolved conversations and the latest base.

GitHub Actions now performs **PR validation only**. The institution-approved deployment path is:

`landing-release → GitHub (version 1) source → CodeBuild → manual approval → CodeDeploy`

The deployment approval lives in CodePipeline's `ApproveLandingProduction` action. This is a deliberate migration of the proposed GitHub deployment job's approval checkpoint, **not approval-free deployment**. Announce/confirm that checkpoint when provisioning. Leave the existing GitHub `landing-production` environment and its protection unchanged; no workflow uses it to deploy in this design. The Mapmory release owner must check CodeRabbit, CI, the source SHA and the latest branch head before approving the AWS action. Never approve a stale execution.

## Configuration and provisioning order

`codedeploy/aws-resources.json` records the desired settings without account credentials. It omits the source OAuth token on purpose. Do not feed it directly to AWS CLI.

1. Inspect existing Mapmory resources and confirm this account/region, the current landing symlink/TLS, the active CodeDeploy agent and the existing backend baseline. Announce the change/risk plan before creating anything.
2. Create **only** the `mapmory-landing` CodeDeploy application and `mapmory-landing-production` deployment group, with all three tags. Use existing `codedeploy-project`; select instances matching both `Name=ec2-mapmory` and `ProjectTeam=Mapmory`. Use in-place, no traffic control, `CodeDeployDefault.AllAtOnce`, rollback on deployment failure. Do not run a deployment yet.
3. Create `mapmory-landing-build` with all three tags and existing `codebuild-project`. Use `aws/codebuild/standard:7.0`, Node 24, small Linux build, no privileged mode/VPC, concurrency 1 and timeout 15 minutes. Source/artifacts are CodePipeline. Buildspec is `landing/buildspec.yml`. Logs use only `/aws/codebuild/project-2026` and a `mapmory-landing` stream prefix. Creating the project alone should not start a build.
4. Preserve the live public `VITE_GA_MEASUREMENT_ID`, `VITE_POSTHOG_KEY`, `VITE_POSTHOG_HOST`, and optional `VITE_API_BASE_URL` as CodeBuild project environment variables. They are public client build settings, never private tokens. Check against the current live bundle; do not silently drop analytics because the GitHub repository variables are unset. Set `VITE_LANDING_VERSION=v3`.
5. Only after this PR's files exist on the reviewed `landing-release` head, create `mapmory-landing-release` using existing `codepipeline-project` and all three tags. Choose V1 / SUPERSEDED to avoid parallel deployments. Select the frontend bucket explicitly, not a new default bucket. Connect **GitHub (version 1)** to the canonical repository's `landing-release` branch using the AWS console authorization flow. Let the user complete OAuth if required; never collect tokens/passwords/MFA in chat or copy another pipeline's masked token. The console-created webhook should detect this branch, with polling disabled; verify both to avoid duplicate or missing runs.
6. Configure source namespace `SourceVariables`. Pass `SOURCE_COMMIT_ID=#{SourceVariables.CommitId}` to CodeBuild as shown in the blueprint. Add the **manual approval action before CodeDeploy**. The creation wizard may not offer approval: do not create an immediately deployable pipeline in that case. Initially omit/disable Deploy, add approval, then enable Deploy only after inspection.
7. Pipeline creation can automatically start its first execution and incur build charges. Announce this before creation. Do not approve production until local/CI tests pass and the exact tested source revision is verified. Backend pipeline `mapmory-backend-pipeline` and group `mapmory-prod` remain untouched.

The EC2 `ec2-project` role is an instance role, not the human console user. It may lack CodePipeline/CodeBuild creation permission even though the signed-in institution user can provision those services. Use the existing console login; do not request a new administrator account or broaden the instance role.

## Build, deploy and verification

Local validation in `landing/`: `npm ci`, `npm run build`, `npm test`. Linux CI and CodeBuild additionally execute the real filesystem activation/rollback fixtures (service/network calls are mocked); Windows skips only those Linux-specific fixtures.

CodeBuild validates that `CODEBUILD_RESOLVED_SOURCE_VERSION` equals the source action's full `SOURCE_COMMIT_ID`, builds and tests before packaging, and refuses to publish a failed build. The bundle root contains only `appspec.yml`, `scripts/activate.sh`, and `client/` (static files including `release.txt`). It never includes backend JARs, `.env` files or server code. CodePipeline retains and passes that exact build artifact to approval and deployment.

The CodeDeploy hook:

- Rejects the wrong application/group, invalid deployment IDs/SHAs, symlinked content and out-of-root previous releases.
- Requires an existing known-good landing release for first-run recovery and serializes activations with `flock`.
- Creates a fresh `/var/www/mapmory/releases/<sha>-<deployment-id>` directory, preserving previous releases.
- Atomically replaces only `/var/www/mapmory/current`, reloads Nginx and verifies local HTTPS **and exact release SHA**.
- Restores the prior symlink on activation/reload/health failures. CodeDeploy also has rollback enabled; a later CodeDeploy rollback gets a new deployment ID, so reusing an old SHA does not collide.
- Never uses an ApplicationStop hook, changes Nginx config, or stops/restarts the backend.

After first approval, verify public `https://map-mory.com/release.txt` against the source SHA, homepage/assets, mobile navigation, Nginx and the unchanged backend PID/health. Public/CDN mismatch needs operator investigation; a successful CodeDeploy local check alone is not a public end-to-end check.

The old `scripts/deploy-ec2.sh` remains available for the already-established manual route. It is not used by CodePipeline. Do not remove SSH secrets or change server access as part of this migration.

## Current rollout state

The live site remains the manually deployed `e91d82c` release. Pipeline code/blueprints do not prove AWS provisioning or a successful automated deployment. Record actual resource IDs, checks and rollout status privately after setup; never claim success until the entire pipeline is observed. Keep PR #243 unmerged while first-deployment prerequisites or CodeRabbit review remain incomplete.
