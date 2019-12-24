# multiple-computer-ffmpeg-render

A program for rendering videos with multiple computers.

## Warning

I built this mostly because I wanted to try to build my own java server so this might break when you use it (that being said I did render over 1k videos with it without it breaking).

## Prerequisites

You have to have [FFmpeg](https://www.ffmpeg.org) installed on your system.

## Args

### Server args

```
-server             runs the program as a server
-port               port on which the server should be ran on
-videos_in_one      how many videos will be concatenated to one (disabled by default)
-s_folder           folder where files will be saved (./files by default)
-r_folder           folder where not yet rendered files are located
-recursive          include files that are in subfolders of the folder specified with -r_folder
-ignore             folders with the name will be ignored (See Ignoring folders example)
-auto_build         builds the render queue on startup rather than when render command is executed
-debug              debug logs
```

### Client args

```
-client        runs the program as a client
-address       address of the server
-port          port of the server
-s_folder      folder where files will be saved (./files by default)
-local         tells the server that the client is on the same computer (reduces file copies)
-debug         debug logs
```

## Commands

These commands can be ran when the program is open.

### Server commands

```
server         tells you info about connected clients
stop           shutsdown the server and clients
render         starts sending videos to clients
queue          print the not yet rendered video queue
add_folder     lets you to add a folder after starting so you don't need to restart the program and reconnect all the clients
```

### Client commands

```
stop     stops the client _after_ the current render is done
```

## Examples

### Server examples

#### Basic usage

`java -jar ffmpegrender-1.0.jar -server -port 7592 -r_folder G:/videos`

The server will send video files to clients from the `G:/videos` folder.

#### Multiple folders

`java -jar ffmpegrender-1.0.jar -server -port 7592 -r_folder G:/videos -r_folder G:/path/to/other/folder`

#### Ignoring folders

`java -jar ffmpegrender-1.0.jar -server -port 7592 -r_folder G:/videos -recursive -ignore ignore_files_under_this`

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

Connects to a server running at `localhost:7592`

`java -jar ffmpegrender-1.0.jar -client -address localhost -port 7592`
