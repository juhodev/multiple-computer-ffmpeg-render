# multiple-computer-ffmpeg-render

A program for rendering videos with multiple computers.

## Warning

* No encryption (for now)
* I built this mostly because I wanted to try to build my own java server so this might break when you use it (that being said I did render over 1k videos with it without it breaking).

## Prerequisites

You have to have [FFmpeg](https://www.ffmpeg.org) installed on your system and it must be added to path.

## Options

### Server options

```
-auto_build         builds the render queue on startup rather than when render command is executed
-debug              debug logs
-help               shows available options
-ignore             folders with the name will be ignored (See Ignoring folders example)
-port               port on which the server should be ran on
-r_folder           folder where not yet rendered files are located
-recursive          include files that are in subfolders of the folder specified with -r_folder
-s_folder           folder where files will be saved (./files by default)
-server             runs the program as a server
-videos_in_one      how many videos will be concatenated to one (disabled by default)
```

### Client options

```
-address            address of the server
-client             runs the program as a client
-debug              debug logs
-help               shows available options
-local              tells the server that the client is on the same computer (reduces file copies)
-port               port of the server
-s_folder           folder where files will be saved (./files by default)
```

## Commands

These commands can be used when the program is running.

### Server commands

```
add_folder     lets you to add a folder after starting so you don't need to restart the program and reconnect all the clients
progress       shows render progress of clients
queue          print the not yet rendered video queue
render         starts sending videos to clients
server         tells you info about connected clients
stop           shutsdown the server and clients
```

### Client commands

```
stop           stops the client _after_ the current render is done
```

## Examples

### Server examples

#### Basic usage

`java -jar multiple-computer-render-1.0.jar -server -port 7592 -r_folder G:/videos`

The server will send video files to clients from the `G:/videos` folder.

#### Multiple folders

`java -jar multiple-computer-render-1.0.jar -server -port 7592 -r_folder G:/videos -r_folder G:/path/to/other/folder`

#### Ignoring folders

`java -jar multiple-computer-render-1.0.jar -server -port 7592 -r_folder G:/videos -recursive -ignore ignore_files_under_this`

All files under folders named `ignore_files_under_this` will be ignored and files in `videos` and `other_videos` will be rendered.

```
G:
├── videos
|   ├── video0.mp4
|   ├── video1.mp4
|   ├── other_videos
|   |   ├── video2.mp4
|   |   ├── video3.mp4
|   |
|   ├── ignore_files_under_this     <-- will be ignored
|   |   ├── video4.mp4     <-- will be ignored
```

### Client examples

#### Basic usage

Connects to a server running at `192.168.1.23:7592`

`java -jar multiple-computer-render-1.0.jar -client -address 192.168.1.23 -port 7592`


#### Local client

`java -jar multiple-computer-render-1.0.jar -client -address localhost -port 7592 -local`