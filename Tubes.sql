USE Sarusun;

CREATE TABLE Users (
    NIK VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50),
    hp VARCHAR(20),
    role VARCHAR(20),
    ownerNIK VARCHAR (20)
);

CREATE TABLE Pemilik (
    nik VARCHAR(20) PRIMARY KEY,
    nama VARCHAR(50),
    alamat VARCHAR(100),
    hp VARCHAR(20)
);

CREATE TABLE Sarusun (
    id VARCHAR(20) PRIMARY KEY,
    tower VARCHAR(10),
    lantai INT,
    nikPemilik VARCHAR(20),
    FOREIGN KEY (nikPemilik) REFERENCES Pemilik(nik)
);

CREATE TABLE IoTDevice (
    sn VARCHAR(20) PRIMARY KEY,
    idSarusun VARCHAR(20),
    status BIT,
    totalAir FLOAT,
    FOREIGN KEY (idSarusun) REFERENCES Sarusun(id)
);

CREATE TABLE WaterUsageLog (
    id INT IDENTITY PRIMARY KEY,
    sn VARCHAR(20),
    date DATE,
    volume FLOAT,
    FOREIGN KEY (sn) REFERENCES IoTDevice(sn)
);


SELECT * FROM Users
SELECT * FROM IoTDevice
SELECT * FROM Pemilik
SELECT * FROM Sarusun
SELECT * FROM WaterUsageLog

DROP TABLE Users