SCA = {
   scope : 'request',
   javaInterface : 'helloworld.HelloWorldService'
}

x = 0;
 
function sayHello(s) {
   x = x + 1;
   return x;
}
