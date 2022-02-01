# Command line utility for DNS-327L ShareCenter

This is basic command line utility written in java to handle the rescan
of the media files on the D'Link's DNS-327L ShareCenter NAS. After some
modification of the media files (adding, deleting, etc.) it is needed to
rescan the library to refresh the catalog which the connecting devices
see, so there would appear everything as expected.

The rescan method is a long procedure, which demand a web login, a lot of
navigation, initiating the rescan, waiting for it than logging out.

This small CLI will make exactly this with a just only a short command:

```shell
$ jsharecenter --host sharecenter.local
```

## Future plans

It would be good to extend the functionality of this tool with the following
features:

- disk info
- disk usage info
- disk health
- disk temperature
- system temperature
- start disk scan
- start checkdisk
- resource info
  - cpu
  - memory
  - io
- other functions...
