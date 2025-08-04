
# Kubernetes 啟動與停止指令

## 啟動所有 K8s 資源
（apply 所有 yaml）

```powershell
kubectl apply -f k8s/
```

## 停止所有 K8s 資源
（刪除所有 yaml 部署的資源）

```powershell
kubectl delete -f k8s/
```

## 刪除所有 Pod（僅刪除 Pod，不影響 PVC/Service/Deployment）
```powershell
kubectl delete pod --all
```

## 刪除所有 PVC（資料會消失）
```powershell
kubectl delete pvc --all
```

## 刪除所有 PV（資料會消失）
```powershell
kubectl delete pv --all
```

# Data Init Script

可用於初始化資料庫（如 Postgres），建議放在 `src/test/resources/test-admin-init.sql`。

```sql
INSERT INTO ROLES (ID, NAME) VALUES (1, 'ROLE_ADMIN');
INSERT INTO ROLES (ID, NAME) VALUES (2, 'ROLE_USER');

INSERT INTO USERS (ID, EMAIL, PASSWORD, USERNAME) VALUES (
  1,
  'ADMIN@123.COM',
  '$2A$10$7BXUNECZXITVYYDWMPLQDUYR10H9CEV6WX3BK9GT8Q9J4JXN/BM8C',
  'ADMIN'
);

INSERT INTO USERS (ID, EMAIL, PASSWORD, USERNAME)  VALUES (
  2,
  'MIN001@123.COM',
  '$2A$10$OM9HAU8AJBRTNUFNG7UQROD3YMIT/YFTETGWS0NWQPS0SXRBHNKUE',
  'MIN001'
);

INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (1, 1);
INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (2, 2);
```
