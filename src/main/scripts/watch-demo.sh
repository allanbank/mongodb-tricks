#! /bin/sh

tmux split-window -v -p 90
tmux split-window -v -p 50
tmux select-pane -t 0

# Wait for the terminals to initialize
sleep 2

tmux send-keys -t 1 "./watcher.sh mongodb://localhost:27017 test test"
tmux send-keys -t 2 "./modifier.sh mongodb://localhost:27017 test test"

read

# Run it.
tmux send-keys -t 1 enter
sleep 5
tmux send-keys -t 2 enter

read

# All done.
tmux send-keys -t 2 C-c enter "exit" enter
tmux send-keys -t 1 C-c enter "exit" enter


