local key = KEYS[1]
local value = ARGV[1]
local expire = ARGV[2]

if redis.call("setnx", key, value) == 1 then
    redis.call("pexpire", key, expire)
    return 1
elseif redis.call("ttl", key) == -1 then
    redis.call("pexpire", key, expire)
    return 0
else return 0
end