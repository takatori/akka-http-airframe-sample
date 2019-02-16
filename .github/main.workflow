workflow "New workflow" {
  on ="push"
  resolves = ["Hello World"]  
}

action "Hello World" {
  uses = "./.github/actions/helloworld/"
  env = {
    MY_NAME = "Mona"
  }
  args = "\"Hello world, I'm $MY_NAME!\""
}
