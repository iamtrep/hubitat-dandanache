# Knockturn Alley - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.0] - 2023-10-28
### Added
- Add `Oppugno` spell to configure attribute reporting

## [1.5.0] - 2023-10-27
### Added
- `Legilimens` spell now also gathers data from Neighbors Table (LQI), Routing Table and Bindings Table

## [1.4.0] - 2023-10-26
### Added
- `Legilimens` spell now also gathers data from Node Descriptor and Node Power Descriptor
- Translate attribute hex value to friendly representations for some known attributes (e.g. Power On Behavior, Temperature, Relative Humidity, etc.)

## [1.3.0] - 2023-10-04
### Added
- Add option to specify manufacturer code when handling Zigbee attributes and when executing Zigbee commands

### Changed
- Change / shuffle some spell names

## [1.2.0] - 2023-09-29
### Added
- Add `Bombarda` spell to execute Zigbee cluster commands

### Changed
- `Legilimens` spell now also discovers all commands that each cluster can receive

## [1.1.0] - 2023-09-28
### Added
- Add `Imperio` spell to write Zigbee cluster atrributes value
- `Scourgify` spell has now the option to remove or keep the raw data

## [1.0.0] - 2023-09-27
### Added
- Initial release
