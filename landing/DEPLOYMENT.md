# Landing CI

`landing-cicd.yml` validates landing-page changes without deploying them. It runs for landing-related pull requests, changes merged into `main`, and manual workflow dispatches.

## CI pipeline

1. Install dependencies with `npm ci`.
2. Build `dist/client`.
3. Run the landing-page tests against the built output.

The workflow does not package a release, access AWS, or connect to EC2. No GitHub environment, AWS credential, or SSH secret is required to run it.

## Deployment

Automatic deployment is intentionally postponed. Production releases must use the separately reviewed manual deployment procedure until the team introduces an AWS-supported deployment pipeline.
