# This script is for getting 100 clients to connnect to our server, instead of us having to open 100 terminals!
# What you'll do is type ./start.sh <number> into terminal. If that number is 10, then it will go through our
# machine_list 10 times, giving use 100 clients

# These three are the variables that need to be changed when the server has changed. The port
# number will probably stay the same. Message frequency... might change.
SERVER_HOSTNAME="phoenix"
SERVER_PORT="41350"
MESSAGE_FREQ="2"

CLASSES="${PWD}/build/classes/java/main"

SCRIPT="cd ${CLASSES}; java -cp . cs455.scaling.client.Client ${SERVER_HOSTNAME} ${SERVER_PORT} ${MESSAGE_FREQ}"

#$1 is the command-line argument specifying how many times it should open the machine list. 
#If 2 is specified, and there are 10 machines on the list, this will open and run on 20 machines
#for j in {1..${1}}

for ((j=1;j<=${1};j++));
do
    COMMAND='gnome-terminal'
    for i in `cat machine_list`
    do
        echo 'logging into '${i}
        OPTION='--tab -e "ssh -t '${i}' '${SCRIPT}'"'
        COMMAND+=" ${OPTION}"
    done
    eval ${COMMAND} &
done
