[Install Docker Desktop](https://docs.docker.com/desktop/) for your platform.

```
$ docker build -t data-engineer-project-template .

# A bunch of Docker output goes here
# Hopefully ending in success

$ docker run -it --rm data-engineer-project-template
Hello world!
Found 15 total games on this date.
Writing CSV file...
Done
```
