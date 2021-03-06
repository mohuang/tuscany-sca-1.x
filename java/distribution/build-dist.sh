#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2006 The Apache Software Foundation.
#  
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  
#       http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------

#   Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
#   reserved.
#
CUR_DIR=`pwd`
cd ..
TUSCANY_HOME=`pwd`
echo Building Tuscany ...
echo $TUSCANY_HOME
mvn -Dtuscany.home=$TUSCANY_HOME $1 clean install -Dmaven.test.skip=true
if [ $? -eq 0 ]
then 
cd $CUR_DIR
echo Creating Tuscany distribution ...
mvn $1 clean install
fi

