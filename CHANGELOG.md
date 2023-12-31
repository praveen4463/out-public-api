## v0.0.3

Enhancements

### Enhancements

1. Added `notifyOnCompletion` parameter to build config.

## v0.0.4

Bug fixes

### Bug fixes

1. Made `notifyOnCompletion` primitive boolean rather than Boolean which caused null exception when
   not passed.

## v0.0.5

Enhancements

### Enhancements

1. Dropped one of the vm zone and kept just one for now. This is done so that we have to manage just
   one zone and don't see the other one when listing. The other zone will still be reattempted in
   provisioner if it fails in the given zone.

## v0.0.6

Enhancements

### Enhancements

1. Added APIDefault
2. Added support for screenshot and driver logs config option

## v0.0.7

Bug fixes

### Bug fixes

1. When shot preference not set, set it to `true`

## v0.0.8

Enhancements

### Enhancements

1. Added support for test detail in api response

## v0.0.9

Enhancements

### Enhancements

1. Renamed requireDetailedResultInResponse to includeDetailedResultInResponse

## v0.1.0

Enhancements

1. Added parallel run capability.
2. Increase proxy timeout to support longer builds.

## v0.1.1

### Bug fixes

1. Fixed the date format in emails and applied a timezone for EST rather than an offset.

## v0.1.2

### Bug fixes

1. Pooling to know the status of a build rather than depending on keep-alive connection.
2. Fixed the test split bug

## v0.1.3

### Enhancements

1. Added build part link to errors.
2. Added start and end date.

### Bug fixes

1. Fixed build ordering bug in build parts.

## v0.1.4

### Bug fixes

1. Fixed a bug in file selector query introduced while building parallel functionality.

## v0.1.5

### Enhancements

1. Added ability to perform mobile emulation and provided more config options for runner options.

## v0.1.6

### Enhancements

1. Added a link to url upon failure in parallel builds.