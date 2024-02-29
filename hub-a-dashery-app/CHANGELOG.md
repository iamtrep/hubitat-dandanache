# Hub-a-Dashery - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0] - 2024-02-29
### Add
- Add grid system to move charts around
- Add Free Mem and CPU Load Avg gauge charts (refresh every 1 minute)
- Reset graph zoom on double-click

## Changed
- Enable data decimation to reduce number of rendered points

### Fixed
- Remove call to non-existing method `fetchHelper()` during install - `@WarlockWeary`
- Fix year reset bug for history charts - `@hubitrep`

## [1.0.0] - 2024-02-26
### Added
- Initial release
