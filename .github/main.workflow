workflow "New workflow" {
  on ="push"
  resolves = ["sbt test"]  
}

action "Hello World" {
  uses = "./.github/actions/helloworld/"
  env = {
    MY_NAME = "Mona"
  }
  args = "\"Hello world, I'm $MY_NAME!\""
}

action "sbt test" {
  uses = "./.github/actions/test/"
  env = {
    SCALA_VERSION = "2.12.7"
    SBT_VERSION = "1.2.8"
  }
}
