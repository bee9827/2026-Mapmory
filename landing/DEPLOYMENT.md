# Landing CI/CD

`landing-cicd.yml` validates every landing-page pull request and deploys only after a landing-related change reaches `main`, or when the workflow is manually dispatched.

## GitHub environment

Create an environment named `landing-production` and add these environment secrets:

- `LANDING_EC2_HOST`: EC2 Elastic IP or SSH hostname
- `LANDING_EC2_USER`: `ubuntu`
- `LANDING_EC2_SSH_KEY`: a private SSH key authorized for the EC2 deploy user
- `LANDING_EC2_KNOWN_HOSTS`: the verified `known_hosts` entry for the EC2 host

Prefer a dedicated deployment key instead of a personal EC2 key. Verify the host fingerprint before saving `LANDING_EC2_KNOWN_HOSTS`.

To prevent an accidental production release, restrict this environment to the `main` branch and configure a required reviewer when the repository plan supports it.

## Pipeline

1. Install dependencies with `npm ci`.
2. Run the existing landing-page tests.
3. Build `dist/client`.
4. Package and retain the static build as a GitHub Actions artifact for seven days.
5. Copy the artifact to EC2 over SSH.
6. Extract it into `/var/www/mapmory/releases/<commit>-<attempt>`.
7. Atomically switch `/var/www/mapmory/current` to the new release.
8. Validate and reload Nginx.
9. Verify the landing page locally on the EC2 origin and publicly over HTTPS.

If the origin verification fails after activation, the deployment script switches `current` back to its previous target and reloads Nginx.
