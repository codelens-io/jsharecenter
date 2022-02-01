# Command line utility for DNS-327L ShareCenter

This is basic command line utility written in java to handle the rescan
of the media files on the D'Link's DNS-327L ShareCenter NAS. After some
modification of the media files (adding, deleting, etc.) it is needed to
rescan the library to refresh the catalog which the connecting devices
see, so there would appear everything as expected.

![d-link_dns-327l_sharecenter_nas](https://user-images.githubusercontent.com/21309032/151985909-14c36300-d49e-443c-b6d4-d52527d5255c.jpg)

The rescan method is a long procedure, which demand a web login, a lot of
navigation, initiating the rescan, waiting for it than logging out.

This small CLI will make exactly this with a just only a short command:

```shell
$ jsharecenter --host sharecenter.local
```

https://user-images.githubusercontent.com/21309032/151986646-6fd17cfd-5e8e-4233-8d68-ec0b2397fd32.mp4

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
