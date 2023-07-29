DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
     UID INT AUTO_INCREMENT,
     AID INT,
     SIN  VARCHAR(255) UNIQUE,
     Email VARCHAR(255) UNIQUE,
     Name VARCHAR(255),
     DoB DATE,
     Occupation VARCHAR(255),
     Password VARCHAR(255),
     Account VARCHAR(255),
     PRIMARY KEY(UID)
);

DROP TABLE IF EXISTS Address;
CREATE TABLE Address (
    AID INT AUTO_INCREMENT,
    Street VARCHAR(255),
    City VARCHAR(255),
    Country VARCHAR(255),
    Postal_code VARCHAR(255),
    PRIMARY KEY(AID)
);
DROP TABLE IF EXISTS Guests;
CREATE TABLE Guests (
    UID INT AUTO_INCREMENT,
    payment_info VARCHAR(255),
    PRIMARY KEY(UID)
);
DROP TABLE IF EXISTS Hosts;
CREATE TABLE Hosts (
    UID INT AUTO_INCREMENT,
    PRIMARY KEY(UID)
);

DROP TABLE IF EXISTS Listings;
CREATE TABLE Listings (
    LID INT AUTO_INCREMENT PRIMARY KEY,
    AID INT,
    UID INT,
    Type VARCHAR(255),
    Longitude DOUBLE,
    Latitude DOUBLE,
    Status VARCHAR(45)
);

DROP TABLE IF EXISTS Calendar;
CREATE TABLE Calendar (
    CID INT AUTO_INCREMENT PRIMARY KEY,
    LID INT,
    Date DATE,
    Price DECIMAL(10,2),
    Availability VARCHAR(255)
);

DROP TABLE IF EXISTS Reservation;
CREATE TABLE Reservation (
    RID INT AUTO_INCREMENT PRIMARY KEY,
    LID INT,
    UID INT,
    Price DOUBLE,
    Availability VARCHAR(255),
    StartDate DATE,
    EndDate DATE,
    Rating INT,
    Comment TEXT
);

DROP TABLE IF EXISTS Category;
CREATE TABLE Category (
    Category_ID INT AUTO_INCREMENT PRIMARY KEY,
    Category_Name VARCHAR(255)
);

DROP TABLE IF EXISTS Amenities;
CREATE TABLE Amenities (
    Amenities_ID INT AUTO_INCREMENT PRIMARY KEY,
    Category_ID INT,
    Amenity_Name VARCHAR(255)
);

DROP TABLE IF EXISTS AmenitiesListing;
CREATE TABLE AmenitiesListing (
    ALID INT AUTO_INCREMENT PRIMARY KEY,
    LID INT,
    Amenities_ID INT
);



