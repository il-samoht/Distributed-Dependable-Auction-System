cd server
javac *.java
rmiregistry &
sleep 2
java FrontEnd