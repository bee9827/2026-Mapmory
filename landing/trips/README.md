# Trips web experiment

Separate metadata-only photo experiment for `https://map-mory.com/trips/`.
Entering `/trips` redirects to `/trips/`, preserving the query string.
The existing homepage and `/recap/` are unchanged.

## Flow and privacy

Select originals → read metadata → read-only classified albums, then survey on the
same screen. No editing, confirmation, location/date review or home-region input.
All undecidable date-only and undated photos share ONE holding album, after the
travel candidates. Original location/date folders and ZIP remain optional secondary views.
Observed location/date evidence and bounded capture-time linkage are labeled separately.
Automatic capture-count inference is paused. With no usable GPS/date anchors, preserve ALL
photos in one holding album, even if a large capture burst exists. These are experimental rules,
not validated travel detection. See [TRIP_GROUPING_PLAN.md](./TRIP_GROUPING_PLAN.md).
Results are memory-only and disappear on refresh. ZIP keeps the original
location/date folder structure; it does not export the travel-candidate grouping.

At the bottom, an OPTIONAL button explores capture patterns using already selected
photos. It never requires reselection or runs automatically. The baseline uses all
selected dated photos; only unclassified GPS-missing photos can become experimental
candidates. Original albums stay intact, and the experimental preview is clearly
labeled as an alternative view of holding photos, not additional photos. Insufficient
evidence stays pending. Summary counts include empty selected days (calendar span
minus active dates), NOT confirmed days without shooting. These gaps are excluded
from mean/median calculations rather than zero-filled.

The user authorized production promotion after local review on 2026-09-23.
Use the reviewed main PR → landing-release PR → existing automatic deployment path.
Do not interpret a successful local build or PR merge alone as verified deployment.

Photos, filenames, timestamps and GPS stay in browser memory. No photo upload,
image analysis or persistent photo database is used. Optional, consent-gated GA4
records aggregate counts and explicit usage steps, never these photo details.
Analytics consent and the team QA flag are stored locally; GA cookies are used
only after consent. Refusal does not restrict the experiment.
The optional Google Forms survey receives only what the participant submits.

The metadata reader is mechanically adapted from `../travel-map-campaign`
(/recap), including File/byte retries, ExifReader fallback and batches of three.
Do not infer capture dates from file modification time. Browser/picker redaction
cannot be repaired by requesting current geolocation.

Trips processes up to 1,000 photos, each up to 50MB. Oversized photos are excluded
before reading any bytes; the other photos continue and the result explicitly
reports the excluded count and that they are absent from the ZIP. If every photo
is oversized, explain why without clearing previous results. Recap's separate 200-photo / 500MB video-selection
validator is not used: Trips keeps File handles and small metadata records, reads
only three files concurrently and prepares one size-bounded ZIP part at a time.
This is a safety boundary, not a guarantee of performance on every phone.

HEIC metadata reading is distinct from preview support: the current browser must
decode the original image to show it. No HEIC/RAW preview decoder is included.

## Local development

`npm ci`, `npm test`, `npm run build`.
`npm run dev` serves the source at `http://127.0.0.1:5174/trips/`.
Use `npm run dev -- --lan` for a same-Wi-Fi phone test; `--port 5175`
selects another port. This server is for local development only.

`npm run sync:recap` refreshes the metadata reader from the sibling Recap
source. Review and test changes to the generated adapter before committing.

The build copies only `src/` to `dist/trips/`; dependencies, tests, hosting
credentials and personal test photos are never packaged.

## Experiment measurement

See [MEASUREMENT_PLAN.md](./MEASUREMENT_PLAN.md) for events, recruitment links,
metadata-missing ratios, Android non-Kakao exclusions and GA4 setup/QA.
Production builds pin the public Trips-only ID `G-P0TDZHRQ6P`; the shared pipeline's
`VITE_GA_MEASUREMENT_ID` is deliberately ignored. The user chose this separate
stream on 2026-09-23, and its enhanced measurement was verified OFF in GA4.
`TRIPS_GA_RELEASE_APPROVED=true` enables the consent-gated production build.
Source development and non-production hosts never track by default. Explicit
local QA (`VITE_GA_CAPTURE_LOCAL=true`) requires its own `VITE_GA_MEASUREMENT_ID`
and may enable `VITE_GA_DEBUG=true`; it never silently inherits the production ID.
No server or shared landing/Recap analytics configuration changes are needed.
Tracking code, console custom definitions and confirmed GA receipt are distinct
checks. Do not announce production measurement until all three have been checked.

## Deployment

Main integration and production promotion are separate. The shared landing
pipeline builds/tests all three surfaces and packages Trips into `client/trips/`.
Before release, separately verify/apply the reviewed `../nginx/trips.conf`
locations to the existing Mapmory server block. The PR and deployment hook do
not apply Nginx configuration. See `../DEPLOYMENT.md`.

## Source

Started from https://github.com/Redish03/mapmory-photo-organizer, then adapted
from upstream snapshot `0d70d7b78515978a8ca3f2f4aba8e48250b26218`
for Mapmory's /recap reader and mobile travel experiment. Vendored dependencies
retain their license notices under `src/vendor/`; geographical dataset credits
are preserved in the UI and bundled data files.
