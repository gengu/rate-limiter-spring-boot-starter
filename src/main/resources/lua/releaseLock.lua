--
-- Created by IntelliJ IDEA.
-- User: junzijian
-- Date: 2017/9/18
-- Time: 15:30
-- To change this template use File | Settings | File Templates.
-- releaseLock

local key = KEYS[1]
local value = ARGV[1]

if redis.call("GET", key) == value then
    redis.call("DEL", key)
    return 1
end
return 0