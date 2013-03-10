#! /bin/sh

tmux split-window -v -p 90
tmux split-window -v -p 50
tmux split-window -v -p 67
tmux split-window -v -p 50
tmux select-pane -t 0


# Wait for the terminals to initialize
sleep 2

tmux send-keys -t 1 "./init-queue.sh mongodb://localhost:27017 test queue"

read

tmux send-keys -t 1 enter

read

tmux send-keys -t 1 "./publish-queue.sh mongodb://localhost:27017 test queue"
tmux send-keys -t 2 "./consume-queue.sh mongodb://localhost:27017 test queue"
tmux send-keys -t 3 "./consume-queue.sh mongodb://localhost:27017 test queue"
tmux send-keys -t 4 "./consume-queue.sh mongodb://localhost:27017 test queue"

read

# Run it.
tmux send-keys -t 1 enter
sleep 5
tmux send-keys -t 2 enter
sleep 1
tmux send-keys -t 3 enter
sleep 1
tmux send-keys -t 4 enter

read

# All done.
tmux send-keys -t 4 C-c enter "exit" enter
tmux send-keys -t 3 C-c enter "exit" enter
tmux send-keys -t 2 C-c enter "exit" enter
tmux send-keys -t 1 C-c enter "exit" enter

