# Changelog

## [Unreleased]

## [2.0.0] 2021-06-23
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5711128.svg)](https://doi.org/10.5281/zenodo.5711128)

### Added
* License header in Java source files
* Added tests

### Fixed

### Changed

## [1.0.0]
### Added
* Configuration option 'removeOAIEnvelope' to store records with or without OAI envelope

### Fixed
* Fixed the timeout settings not applying when listing record identifiers with a resumption token, fixed not getting all sets.

### Changed
* Cleaned up code 


## [0.0.2-SNAPHSOT]   


* Harvesting of OAI servers that have no sets specified
* Apart from standard application logging at class level, the harvester has a special logger (hlog), that prints out status information on the harvesting process only. It contains explicit information on e.g. the start, end and result of a harversting run. 
* Configurations for 
    * CSDA
    * DANS
    * DBK
    * DNA
    * EKKE
    * FSD
    * NSD
    * SND
    * UKDA
 

### Changed 
n/a

### Deprecated
n/a

### Removed
n/a

### Fixed
n/a

### Security 
* added letsencrypt certs - to be imported into the JRE when harvesting OAI servers available via HTTPS only and using letsencrypt SSL certificates
