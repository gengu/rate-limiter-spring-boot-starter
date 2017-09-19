local key = KEYS[1]
local value = ARGV[1]

if redis.call("GET", key) == value then
    redis.call("DEL", key)
    return 1
else return 0
end