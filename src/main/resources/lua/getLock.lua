local key = KEYS[1]
local value = ARGV[1]
local expire = ARGV[2]

if redis.call("setnx", key, value) == 1 then
    if redis.call("pexpire", key, expire) == 1 then
        return 1
    end
    return 0
elseif redis.call("ttl", key) == -1 then
    redis.call("pexpire", key, expire)
end
return 0

