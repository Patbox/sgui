# SGui (Server Gui)
It's a small, jij-able library that allows creation of server side guis.

## Usage:
Add it to your dependencies like this:

```
repositories {
	maven { url 'https://maven.nucleoid.xyz' }
}

dependencies {
	modImplementation include("eu.pb4:sgui:[TAG]")
}
```

After that you are ready to go! You can use SimpleGUI and other classes directly for simple ones or extend
them for more complex guis.