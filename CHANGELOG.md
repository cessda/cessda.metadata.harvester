# Changelog

## [Unreleased]

## [2.0.0] 2021-06-23

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5711128.svg)](https://doi.org/10.5281/zenodo.5711128)

### Added

* The harvester can now accept configuring repositories with different metadata prefixes ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
* Added the ability to harvest each repository in parallel ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
* Log OAI-PMH errors ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
* Output both wrapped and unwrapped metadata records ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
* Added JSON logging support ([#12](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/12))
* Handle records marked as deleted in an OAI-PMH repository ([#18](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/))

### Fixed

* Handle the NSD/NESSTAR date format ([#19](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/19))

### Changed

* Set discovery is now disabled by default ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
* The harvester now exits after completion of a harvest
* Use the Java 11 HTTP client to perform HTTP requests ([#17](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/17))
* Improved the test coverage of the harvester ([#16](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/16))
* Updated OpenJDK to 17 ([#21](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/21))

### Removed

* Removed support for sending email notifications when errors are encountered
* Removed internal scheduling support
* Removed Spring Boot Admin support

## [1.0.1]

### Added
* License header in Java source files
* Added tests

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
