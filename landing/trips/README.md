# Trips web experiment

Separate metadata-only photo experiment for `https://map-mory.com/trips/`.
Entering `/trips` redirects to `/trips/`, preserving the query string.
The existing homepage and `/recap/` are unchanged.

## Flow and privacy

Select originals → location folders (date view below) → choose a home region
from photo albums → review travel candidates. ZIP keeps the original location/date
folder structure; it does not export the travel-candidate grouping.

Photos, filenames, timestamps and GPS stay in browser memory. No photo upload,
image analysis, analytics SDK or persistent browser database is used.
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
