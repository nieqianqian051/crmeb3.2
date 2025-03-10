<?php

namespace crmeb\services;

use think\exception\ValidateException;
use think\facade\Cache;

class LockService
{
    /**
     * @param $key
     * @param $fn
     * @param int $ex
     * @return mixed
     * @author 吴汐
     * @email 442384644@qq.com
     * @date 2023/03/01
     */
    public function exec($key, $fn, int $ex = 6)
    {
        try {
            if ($this->lock($key, $key, $ex))
            	return $fn();
			else throw new ValidateException('请求太过频繁，请稍后再试');
        } finally {
            $this->unlock($key, $key);
        }
    }

    public function tryLock($key, $value = '1', $ex = 6)
    {
        return Cache::store('redis')->handler()->set('lock_' . $key, $value, ["NX", "EX" => $ex]);
    }

    public function lock($key, $value = '1', $ex = 6)
    {
        if ($this->tryLock($key, $value, $ex)) {
            return true;
        } else {
			return false;
		}
    }

    public function unlock($key, $value = '1')
    {
        $script = <<< EOF
if (redis.call("get", "lock_" .. KEYS[1]) == ARGV[1]) then
	return redis.call("del", "lock_" .. KEYS[1])
else
	return 0
end
EOF;
        return Cache::store('redis')->handler()->eval($script, [$key, $value], 1) > 0;
    }
}