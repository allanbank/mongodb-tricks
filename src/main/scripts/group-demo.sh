#! /bin/sh

tmux split-window -v -p 90
tmux split-window -v -p 75
tmux split-window -v -p 67
tmux split-window -v -p 50
tmux select-pane -t 0


# Wait for the terminals to initialize
sleep 2

tmux send-keys -t 1 "./watcher.sh mongodb://localhost:27017 test group"
tmux send-keys -t 2 "./group-member.sh mongodb://localhost:27017 test group"
tmux send-keys -t 3 "./group-member.sh mongodb://localhost:27017 test group"
tmux send-keys -t 4 "./group-member.sh mongodb://localhost:27017 test group"

read

# Run it.
tmux send-keys -t 1 enter
sleep 1
tmux send-keys -t 2 enter
sleep 1
tmux send-keys -t 3 enter
sleep 1
tmux send-keys -t 4 enter

read

# Kill nicely.
tmux send-keys -t 4 C-c enter

read

# Kill not so nicely.

tmux send-keys -t 3 C-z enter "kill -9 %1" enter

read

# All done.
tmux send-keys -t 4 C-c enter "exit" enter
tmux send-keys -t 3 C-c enter "exit" enter
tmux send-keys -t 2 C-c enter "exit" enter
tmux send-keys -t 1 C-c enter "exit" enter

