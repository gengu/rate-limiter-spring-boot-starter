--
-- Created by IntelliJ IDEA.
-- User: wb-lz260260
-- Date: 2017/9/18
-- Time: 15:54
-- To change this template use File | Settings | File Templates.
-- getLock

-- lua的索引从1开始
local key = KEYS[1]
local value = ARGV[1]
local expire = ARGV[2]

--得到锁
if redis.pcall("SET", key, value, "NX", "PX", expire) then
    return 1
    --检查过期时间, 并在必要时对其更新
elseif redis.call("TTL", key) == -1 then
    redis.call("PEXPIRE", key, expire)
    return 0
else
    return 0
end