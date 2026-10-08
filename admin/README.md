# Mapmory Admin

React + TypeScript web prototype for Mapmory operations.

```bash
cd admin
npm install
npm run dev
```

The current screens use local demo data. Dashboard metrics, member data, feedback storage, and status updates are not connected to the Java backend yet. The admin UI and `/api/v1/admin/**` endpoints currently have no login or access control.

## Screens

- Dashboard: member, travel-record, and feedback overview
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
