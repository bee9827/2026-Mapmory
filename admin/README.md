# Mapmory Admin

React + TypeScript web prototype for Mapmory operations.

```bash
cd admin
npm install
npm run dev
```

The dashboard summary is connected to the Java backend. Member management and service feedback still use local demo data. The admin UI and `/api/v1/admin/**` endpoints currently have no login or access control.

Set `VITE_API_BASE_URL` to the backend API base URL if it differs from `http://localhost:8080/api/v1`. The dashboard requests `GET /admin/dashboard` with `from` and `to` date parameters.

## Screens

- Dashboard: member and travel-record totals and selected-period counts (Java API)
- Members: search, provider filter, and member detail drawer
- Service feedback: status filter and internal status updates

Feedback states and UI labels:

| UI label | API code |
| --- | --- |
| 미확인 | `UNCONFIRMED` |
| 반영 중 | `IN_PROGRESS` |
| 반영 완료 | `COMPLETED` |
| 보류 | `ON_HOLD` |
| 미반영 | `NOT_IMPLEMENTED` |
