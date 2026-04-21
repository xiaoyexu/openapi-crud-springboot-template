IF schema_id ('DEV' ) is null
Begin
 EXEC('CREATE SCHEMA DEV');
End
GO

IF OBJECT_ID ('DEV.STUDENTS') IS NULL
BEGIN
CREATE TABLE DEV.STUDENTS(
  ID                      VARCHAR(50) NOT NULL,
  CONSTRAINT PK_DOCUMENT PRIMARY KEY (ID),
  NAME                    VARCHAR(200) NOT NULL,
  AGE                     INT,
  CREATED_BY              VARCHAR(200) NOT NULL,
  CREATED_AT              DATETIME NOT NULL,
  UPDATED_BY              VARCHAR(200) NOT NULL,
  UPDATED_AT              DATETIME NOT NULL
);

INSERT INTO DEV.STUDENTS(ID,NAME,AGE,CREATED_BY,CREATED_AT,UPDATED_BY,UPDATED_AT) VALUES
('S000001','Name 1', 18,  'system', '2025-06-17','system','2025-06-17'),
('S000002','Name 2', 19,  'system', '2025-06-17','system','2025-06-17'),
('S000003','Name 3', 19,  'system', '2025-06-17','system','2025-06-17'),
('S000004','Name 4', 18,  'system', '2025-06-17','system','2025-06-17'),
('S000005','Name 5', 22,  'system', '2025-06-17','system','2025-06-17'),
('S000006','Name 6', 23,  'system', '2025-06-17','system','2025-06-17');

END

