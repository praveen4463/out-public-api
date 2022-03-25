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