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

## [3.6.0] - 2024-06-17

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.12089946.svg)](https://doi.org/10.5281/zenodo.12089946)

### Changed

* Harvest records to a temporary file, throw an `IOException` if the `HttpClient` is interrupted ([PR-48](https://github.com/cessda/cessda.metadata.harvester/pull/48))
* Updated SND endpoint domain, metadata prefix and OAI-PMH set ([PR-50](https://github.com/cessda/cessda.metadata.harvester/pull/50))
* Updated OpenJDK to version 21 ([#658](https://github.com/cessda/cessda.cdc.versions/issues/658))
* Updated SQAaaS software quality badge ([#661](https://github.com/cessda/cessda.cdc.versions/issues/661))

### Fixed

* Fixed only harvesting 100 records from SND ([#655](https://github.com/cessda/cessda.cdc.versions/issues/655))

## [3.4.0] - 2023-08-29

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8277067.svg)](https://doi.org/10.5281/zenodo.8277067)

**NOTICE**: The location of the harvesting configuration when running on the CESSDA Cloud Platform has changed, and can be found at <https://github.com/cessda/cessda.cdc.aggregator.deploy/blob/main/charts/harvester/config/config.yaml>. Changing application-cdc.yaml in this repository will have no effect. 

### Changed

- Documented the `Repo` configuration class in the README and JavaDocs ([#569](https://github.com/cessda/cessda.cdc.versions/issues/569))
- Revised the documentation in [README.md](README.md), removing references to properties that no longer have any effect ([PR-29](https://github.com/cessda/cessda.metadata.harvester/pull/29))
- Stream records directly to disk, improving performance and reducing memory usage ([#565](https://github.com/cessda/cessda.cdc.versions/issues/565))

### Removed

- Removed the ability to "unwrap" records ([#565](https://github.com/cessda/cessda.cdc.versions/issues/565))

## [3.3.0] - 2023-06-13

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8021252.svg)](https://doi.org/10.5281/zenodo.8021252)

### Added

- Added retry logic for when HTTP requests fail due to remote server
  unavailability ([#529](https://github.com/cessda/cessda.cdc.versions/issues/529))

### Changed

- Changed AUSSDA's setspec ([#439](https://github.com/cessda/cessda.cdc.versions/issues/439))
- Improved the harvester's SQL rating ([#489](https://github.com/cessda/cessda.cdc.versions/issues/489))

### Fixed

- Fixed not parsing OAI-PMH metadata formats if a namespace prefix was used
  (i.e. if `oai:request` was used instead of `request`) by only comparing the
  local name in a namespace aware context
  ([PR-9](https://github.com/cessda/cessda.metadata.harvester/pull/9))

### Security

- Removed Apache Xalan due to [CVE-2022-34169](https://github.com/advisories/GHSA-9339-86wc-4qgf)

## [3.2.1] - 2023-02-07

### Added

- Add new Sikt and ESS endpoints ([#505](https://github.com/cessda/cessda.cdc.versions/issues/505))

### Changed

- Renamed the main package from `eu.cessda.eqb.harvester` to
  `eu.cessda.oaiharvester`
  ([PR-2](https://github.com/cessda/cessda.metadata.harvester/pull/2))

### Removed

- Removed DNA from the list of repositories ([#524](https://github.com/cessda/cessda.cdc.versions/issues/524))

### Fixed

- Fixed not being able to parse OAI responses that have a namespace
  prefix defined
  ([#505](https://github.com/cessda/cessda.cdc.versions/issues/505))

## [3.1.1] - 2022-11-08

### Fixed

- Replace path characters considered invalid by the current filesystem
  with "-" when creating setSpec directories
  ([#480](https://github.com/cessda/cessda.cdc.versions/issues/480))

## 3.0.2 - 2022-09-06

### Fixed

- Fixed not logging in JSON ([#450](https://github.com/cessda/cessda.cdc.versions/issues/450))

## [3.0.0] - 2022-06-07

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6577757.svg)](https://doi.org/10.5281/zenodo.6577757)

### Added

- Added new Progedo endpoint [#403](https://github.com/cessda/cessda.cdc.versions/issues/403)
- Added metrics instrumentation for UI + API Search Queries [#393](https://github.com/cessda/cessda.cdc.versions/issues/393)

### Changed

- Updated harvester configuration to match new
  harvesting/validation/indexing pipeline model
  [#423](https://github.com/cessda/cessda.cdc.versions/issues/423)
- Refactored harvesting/validation/indexing pipeline configuration [#409](https://github.com/cessda/cessda.cdc.versions/issues/409)
- Improved the test coverage of the harvester [#16](https://bitbucket.org/cessda/cessda.metadata.harvester/issues/16)

### Fixed

- Ensured all available endpoints are in the pipeline [#411](https://github.com/cessda/cessda.cdc.versions/issues/411)
- Workaround for XML schema violations caused by incorrect DDI serialisation [#388](https://github.com/cessda/cessda.cdc.versions/issues/388)

## 2.0.0 - 2021-11-25

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.5711128.svg)](https://doi.org/10.5281/zenodo.5711128)

### Added

- The harvester can now accept configuring repositories with different
  metadata prefixes
  ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- Added the ability to harvest each repository in parallel ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- Log OAI-PMH errors ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
- Output both wrapped and unwrapped metadata records ([#14](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/14))
- Added JSON logging support ([#12](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/12))
- Handle records marked as deleted in an OAI-PMH repository ([#18](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/18))
- Delete orphaned records, which are records present locally but are
  not advertised in the source repository, after full runs
  ([#25](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/25))

### Fixed

- Correctly handle the NSD/NESSTAR date format ([#19](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/19))

### Changed

- Set discovery is now disabled by default ([#11](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/11))
- The harvester now exits after completion of a harvest
- Use the Java 11 HTTP client to perform HTTP requests ([#17](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/17))
- Improved the test coverage of the harvester ([#16](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/16))
- Updated OpenJDK to 17 ([#21](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/issues/21))

### Removed

- Removed support for sending email notifications when errors are encountered
- Removed internal scheduling support
- Removed Spring Boot Admin support

## 1.0.1

### Added

- License header in Java source files
- Added tests

## [1.0.0]

### Added

- Configuration option 'removeOAIEnvelope' to store records with or without OAI envelope

### Fixed

- Fixed the timeout settings not applying when listing record
  identifiers with a resumption token, fixed not getting all sets.

### Changed

- Cleaned up code

## [0.0.2]

- Harvesting of OAI servers that have no sets specified
- Apart from standard application logging at class level, the
  harvester has a special logger (hlog), that prints out status
  information on the harvesting process only. It contains explicit
  information on e.g. the start, end and result of a harversting run.
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

### Changed

n/a

### Deprecated

n/a

### Removed

n/a

### Fixed

n/a

### Security

- added letsencrypt certs - to be imported into the JRE when
  harvesting OAI servers available via HTTPS only and using
  letsencrypt SSL certificates

[3.6.0]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.6.0
[3.4.0]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.4.0
[3.3.0]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.3.0
[3.2.1]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.2.1
[3.1.1]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.1.1
[3.0.0]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/3.0.0
[1.0.0]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/1.0.0
[0.0.2]: https://github.com/cessda/cessda.metadata.harvester/releases/tag/0.0.2
