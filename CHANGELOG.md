# Changelog

All notable changes to Metadata Harvester will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

*For each release, use the following sub-sections:*

- *Added (for new features)*
- *Changed (for changes in existing functionality)*
- *Deprecated (for soon-to-be removed features)*
- *Removed (for now removed features)*
- *Fixed (for any bug fixes)*
- *Security (in case of vulnerabilities)*

## [3.0.2] - 2022-09-06

## Fixed [3.0.2]

- Fixed not logging in JSON ([#450](https://bitbucket.org/cessda/cessda.cdc.versions/issues/450))

## [3.0.0] - 2022-06-07

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6577757.svg)](https://doi.org/10.5281/zenodo.6577757)

### Added [3.0.0]

- Added new Progedo endpoint [#403](https://bitbucket.org/cessda/cessda.cdc.versions/issues/403)
- Added metrics instrumentation for UI + API Search Queries [#393](https://bitbucket.org/cessda/cessda.cdc.versions/issues/393)

### Changed [3.0.0]

- Updated harvester configuration to match new harvesting/validation/indexing
  pipeline model [#423](https://bitbucket.org/cessda/cessda.cdc.versions/issues/423)
- Refactored harvesting/validation/indexing pipeline configuration [#409](https://bitbucket.org/cessda/cessda.cdc.versions/issues/409)
- Improved the test coverage of the harvester [#16](https://bitbucket.org/cessda/cessda.metadata.harvester/issues/16)

### Fixed [3.0.0]

- Ensured all available endpoints are in the pipeline [#411](https://bitbucket.org/cessda/cessda.cdc.versions/issues/411)
- Workaround for XML schema violations caused by incorrect DDI serialisation
  [#388](https://bitbucket.org/cessda/cessda.cdc.versions/issues/388)

## [2.0.0] 2021-11-25

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5711128.svg)](https://doi.org/10.5281/zenodo.5711128)

### Added [2.0.0]

- The harvester can now accept configuring repositories with different metadata
  prefixes ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- Added the ability to harvest each repository in parallel ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- Log OAI-PMH errors ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
- Output both wrapped and unwrapped metadata records ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
- Added JSON logging support ([#12](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/12))
- Handle records marked as deleted in an OAI-PMH repository ([#18](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/18))
- Delete orphaned records, which are records present locally but are not
  advertised in the source repository,
  after full runs ([#25](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/25))

### Fixed [2.0.0]

- Correctly handle the NSD/NESSTAR date format ([#19](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/19))

### Changed [2.0.0]

- Set discovery is now disabled by default ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- The harvester now exits after completion of a harvest
- Use the Java 11 HTTP client to perform HTTP requests ([#17](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/17))
- Improved the test coverage of the harvester ([#16](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/16))
- Updated OpenJDK to 17 ([#21](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/21))

### Removed [2.0.0]

- Removed support for sending email notifications when errors are encountered
- Removed internal scheduling support
- Removed Spring Boot Admin support

## [1.0.1]

### Added [1.0.1]

- License header in Java source files
- Added tests

## [1.0.0]

### Added [1.0.0]

- Configuration option 'removeOAIEnvelope' to store records with or without OAI envelope

### Fixed [1.0.0]

- Fixed the timeout settings not applying when listing record identifiers with
  a resumption token, fixed not getting all sets.

### Changed [1.0.0]

- Cleaned up code

## [0.0.2-SNAPHSOT]

- Harvesting of OAI servers that have no sets specified
- Apart from standard application logging at class level, the harvester has a
  special logger (hlog),
  that prints out status information on the harvesting process only.
  It contains explicit information on e.g. the start, end and result of a
  harvesting run.
- Configurations for
- CSDA
- DANS
- DBK
- DNA
- EKKE
- FSD
- NSD
- SND
- UKDA

### Changed [0.0.2]

n/a

### Deprecated [0.0.2]

n/a

### Removed [0.0.2]

n/a

### Fixed [0.0.2]

n/a

### Security [0.0.2]

- added letsencrypt certs - to be imported into the JRE when harvesting OAI servers
  available via HTTPS only and using Letsencrypt SSL certificates
