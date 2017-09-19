--
-- Created by IntelliJ IDEA.
-- User: junzijian
-- Date: 2017/9/18
-- Time: 15:30
-- To change this template use File | Settings | File Templates.
-- getLock2 分步执行

local key = KEYS[1]
local value = ARGV[1]
local expire = ARGV[2]

if redis.call("setnx", key, value) == 1 then
    if redis.call("pexpire", key, expire) == 1 then
        return 1
    end
elseif redis.call("ttl", key) == -1 then
    redis.call("pexpire", key, expire)
end
return 0



--[[
local key = KEYS[1]
local value = ARGV[1]
local expire = ARGV[2]

if redis.call("setnx", key, value) == 1 then
redis.call("pexpire", key, expire)
return 1
elseif redis.call("ttl", key) == -1 then
redis.call("pexpire", key, expire)
end
return 0
]]



