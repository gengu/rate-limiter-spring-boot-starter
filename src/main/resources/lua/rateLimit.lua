--
-- Created by IntelliJ IDEA.
-- User: junzijian
-- Date: 2017/9/21
-- Time: 15:42
-- To change this template use File | Settings | File Templates.
-- rateLimit

local key = KEYS[1]
local value = 1
local limit = tonumber(ARGV[1])
local pExpire = ARGV[2]

if redis.call("SET", key, value, "NX", "PX", pExpire) then
    return 1
else
    if redis.call("TTL", key) == -1 then
        redis.call("PEXPIRE", key, pExpire)
    end
    if redis.call("INCR", key) < limit then
        return 1
    end
end
return 0


