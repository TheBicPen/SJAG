#!/bin/sh

set -e  # exit on any command failing

get()
{
    curl --silent --max-time 5 --output /dev/null -f "$1"
}

expect_false()
{
    $@ && exit 1 || echo "Assertion passed: $@"
}

expect_true()
{
    $@ && echo "Assertion passed: $@" || exit 1
}

sleep 5 # add better health check elsewhere

#### No args test
expect_false get 'http://gateway-no-args:8000'
expect_false get 'http://gateway-no-args:8000/'
expect_true get 'http://gateway-no-args:8000/server8000'
expect_true get 'http://gateway-no-args:8000/server8000/'
expect_true get 'http://gateway-no-args:8000/server8000/hello.html'
expect_false get 'http://gateway-no-args:8000/server9000/hello.html'
expect_false get 'http://gateway-no-args:8000/server8000/no-such-file.html'


#### Supplied ports manually test
expect_false get 'http://gateway-default-ports:8000'
expect_false get 'http://gateway-default-ports:8000/'
expect_true get 'http://gateway-default-ports:8000/server8000'
expect_true get 'http://gateway-default-ports:8000/server8000/'
expect_true get 'http://gateway-default-ports:8000/server8000/hello.html'
expect_false get 'http://gateway-default-ports:8000/server9000/hello.html'
expect_false get 'http://gateway-default-ports:8000/server8000/no-such-file.html'


#### Changed default listen port test: gateway7000
expect_true get 'http://gateway7000:7000/server8000/hello.html'
expect_false get 'http://gateway7000:8000/server8000/hello.html'

#### Changed default output port test: gateway-route-9000
expect_true get 'http://gateway-route-9000:8000/server9000'
expect_true get 'http://gateway-route-9000:8000/server9000/hello.html'
expect_false get 'http://gateway-route-9000:8000/server9000/no-such-file.html'

#### Changed default output port, override: gateway-route-override
expect_true get 'http://gateway-route-override:8000/server8000'
expect_true get 'http://gateway-route-override:8000/server8000/'
expect_true get 'http://gateway-route-override:8000/server8000/hello.html'
expect_true get 'http://gateway-route-override:8000/server9000'
expect_true get 'http://gateway-route-override:8000/server9000/'
expect_true get 'http://gateway-route-override:8000/server9000/hello.html'
expect_false get 'http://gateway-route-override:8000/server8000/no-such-file.html'

echo 'Success!'
exit 0