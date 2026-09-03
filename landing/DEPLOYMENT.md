# Landing release

## Branch and protection

- Canonical repository: `woowacourse-teams/2026-Mapmory` (`upstream`), not the personal fork.
- `landing-release` is the long-lived **landing-only production release** branch. Its initial snapshot is `e91d82c727878da8ab05bee1f330f348b3542df3` from `develop`; its landing tree is identical to the tested/reviewed PR #240 head `14219679b59f12526d6abe72c355dda73b46d948`.
- Subsequent releases go through a PR to `landing-release`. Use a merge commit and keep the source branch. Never force-push or delete the release branch.
- Active ruleset: [Landing release protection](https://github.com/woowacourse-teams/2026-Mapmory/rules/22160463). It prohibits deletion/force pushes, requires a PR, resolved review threads, and the GitHub Actions check `Landing release validation` against the latest base. No bypass actors are configured. PR approval count follows the existing repository convention (0); the separate deployment approval remains required.
- Keep app and backend releases separate. This workflow never updates `main`, `develop`, or `backend-release`, builds the backend, or restarts the backend application.

## Validation and deployment

The existing `landing-cicd.yml` remains validation-only for ordinary landing changes. `landing-release.yml` runs on **every** PR to `landing-release` without a path filter so its required check cannot remain pending for docs-only changes.

1. Run `npm ci`, `npm run build`, then `npm test` in `landing/`. Build comes before tests because packaging tests inspect generated files.
2. Request CodeRabbit review and address actionable findings before merging. A skipped/limited bot review is not a completed review.
3. Merge the validated PR into `landing-release`. Its push rebuilds and tests the exact release SHA, writes `release.txt`, and packages **only** `dist/client`.
4. The `landing-production` environment requires the existing `bee9827` approval and only permits the `landing-release` branch. PRs and fork workflows cannot deploy. Do not remove the approval gate.
5. After approval, upload the tested artifact over SSH with pinned host keys and run `landing/scripts/deploy-ec2.sh`. Deployments are serialized, are not interrupted by a newer run, and check that their SHA is still the release branch head before upload.
6. The script activates `/var/www/mapmory/releases/<sha>-<run-id>-<attempt>` through `/var/www/mapmory/current`. It validates Nginx and checks local HTTPS; Nginx config/local health failures restore the previous symlink when available.
7. The workflow checks `https://map-mory.com/release.txt` equals the tested SHA. A public/CDN mismatch fails the job and needs operator investigation; that final public check does not automatically roll back the server.

`workflow_dispatch` is provided for retrying the current release, but GitHub only exposes manual dispatch once this workflow also exists on the default branch. Until that reviewed integration happens, merge-to-release push triggers and the Actions **Re-run jobs** control are the supported entry points. Every retry still requires the environment gate and head-SHA check.

## First deployment prerequisites — do not guess these

The historical EC2 script is retained, but its existence does **not** prove the live server has been provisioned. Confirm the landing host/account, Nginx `map-mory.com` virtual host, TLS, and document root `/var/www/mapmory/current` before approving the first run. The account needs the existing script's sudo permissions. Do not provision infrastructure, change DNS, or reuse backend credentials without explicit confirmation.

Register these secrets in **Settings → Environments → landing-production** using a trusted secret-entry method (never commit them or paste private keys in chat):

| Secret | Value |
| --- | --- |
| `LANDING_EC2_HOST` | Confirmed landing EC2 hostname or IPv4 address, SSH port 22 |
| `LANDING_EC2_USER` | Confirmed deployment account |
| `LANDING_EC2_SSH_KEY` | Deployment account's private SSH key |
| `LANDING_EC2_KNOWN_HOSTS` | Host key entry independently verified against the server |

As of setup on 2026-09-03 these four secrets are absent. Repository secrets named `EC2_HOST`/`EC2_SSH_KEY` are deliberately **not** used as an implicit fallback.

Optional public build settings belong in **repository variables**, because validation/build happens before the environment approval: `VITE_GA_MEASUREMENT_ID`, `VITE_API_BASE_URL`, `VITE_POSTHOG_KEY`, `VITE_POSTHOG_HOST`. All `VITE_*` values are bundled into public client JavaScript; never put a private server/API token there. Existing source defaults apply when values are unset.

## Recovery

- Preserve previous release directories; this workflow does not prune them.
- To restore older content through the normal process, create a revert PR to `landing-release`, validate/review it, merge, and approve its new deployment.
- If the first deployment has no previous `current` symlink, there is no previous release for the script to restore. First-release server readiness must be verified by the operator before approval.
- A failed final public check may be caching, routing, or server state. Inspect the recorded release SHA and server symlink before taking recovery action; an HTTP 200 alone is not proof of the deployed version.
