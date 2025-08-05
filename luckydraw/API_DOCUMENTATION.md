# API Documentation

## AuthController

### POST /api/auth/login
- **Description:** User login, returns JWT token.
- **Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```
- **Response:**
  - 200 OK
    ```json
    {
      "token": "string",
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ROLE_ADMIN"]
    }
    ```
  - 401/403 Unauthorized

---

### POST /api/auth/user
- **Description:** Register new user (ADMIN only).
- **Header:** `Authorization: Bearer <JWT>`
- **Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": ["user" or "admin"]
}
```
- **Response:**
  - 200 OK
    ```json
    {
      "message": "User registered successfully!"
    }
    ```
  - 400/409 Username or email already exists

---

## LotteryController

### POST /api/lottery/activity
- **Description:** Create activity (ADMIN only).
- **Header:** `Authorization: Bearer <JWT>`
- **Request Body:**
```json
{
  // Activity object fields
}
```
- **Response:**
  - 200 OK
    ```json
    {
      "message": "活動建立成功",
      "data": { /* Activity */ }
    }
    ```

---

### POST /api/lottery/prizes/{activityId}
- **Description:** Set prizes for activity (ADMIN only).
- **Header:** `Authorization: Bearer <JWT>`
- **Request Body:**
```json
[
  // Array of Prize objects
]
```
- **Response:**
  - 200 OK
    ```json
    {
      "message": "獎品設定成功",
      "data": [ /* Prize */ ]
    }
    ```

---

### POST /api/lottery/draw/{activityId}?times={n}
- **Description:** Draw lottery (USER only).
- **Header:** `Authorization: Bearer <JWT>`
- **Request Param:** `times` (number of draws)
- **Response:**
  - 200 OK
    ```json
    [
      "A", "B", "NO_PRIZE", ...
    ]
    ```

---

> For detailed model fields, please refer to the corresponding Java classes.
