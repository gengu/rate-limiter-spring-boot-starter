--
-- Created by IntelliJ IDEA.
-- User: junzijian
-- Date: 2017/9/18
-- Time: 15:30
-- To change this template use File | Settings | File Templates.
-- getLock 一步执行

-- lua的索引从1开始
local key = KEYS[1]
local value = ARGV[1]
local pExpire = ARGV[2]

--得到锁
if redis.call("SET", key, value, "NX", "PX", pExpire) then
    return 1
    --检查过期时间, 并在必要时对其更新
elseif redis.call("TTL", key) == -1 then
    redis.call("PEXPIRE", key, pExpire)
end
return 0
