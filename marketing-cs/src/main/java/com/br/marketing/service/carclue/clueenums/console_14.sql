select count(1)
from (select cell_sha256_code_list as cell
      from b_xiecheng_colliding_data_loop_cycle
      where (release_time >= "2025-01-24 00:00:00" and release_time <= "2025-01-24 23:59:59")
        and info = "退订用户，不可短信营销"
        and is_delete = 0) cycle
         inner join (select id, cell
                     from b_xiecheng_colliding_3710058_20250116000000_4891
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250116000000_7118
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250116000000_5732
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250116000000_3314
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250108100000_5189
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250108030000_6741
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_5812
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_2369
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_7647
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_3191
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0) score on score.cell = cycle.cell

select count(1)
from (select cell_sha256_code_list as cell
      from b_xiecheng_colliding_data_loop_cycle
      where (release_time >= "2025-01-24 00:00:00" and release_time <= "2025-01-24 23:59:59")
        and info = "退订用户，不可短信营销"
        and is_delete = 0) cycle
         inner join (select id, cell
                     from b_xiecheng_colliding_3710058_20250108100000_5189
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_8742
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_2411
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_3191
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_7647
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_2369
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_5812
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_4378
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250108030000_6741
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_1333
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0) score
                    on score.cell = cycle.cell

select count(1)
from (select id, cell
      from b_xiecheng_colliding_3710058_20250108100000_5189
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_8742
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_2411
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_3191
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_7647
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_2369
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_5812
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_4378
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250108030000_6741
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_1333
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0) score
         inner join (select cell_sha256_code_list as cell
                     from b_xiecheng_colliding_data_loop_cycle
                     where (release_time >= "2025-01-24 00:00:00" and release_time <= "2025-01-24 23:59:59")
                       and info = "退订用户，不可短信营销"
                       and is_delete = 0) cycle on score.cell = cycle.cell


select count(1)
from (select id, cell
      from b_xiecheng_colliding_3710058_20250108100000_5189
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_8742
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_2411
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_3191
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_7647
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_2369
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_5812
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_4378
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250108030000_6741
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_1333
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0) a

select count(1)
    from b_xiecheng_colliding_data_loop_cycle
where (release_time >= "2025-01-24 00:00:00" and release_time <= "2025-01-24 23:59:59")
  and info = "退订用户，不可短信营销"
  and is_delete = 0



select count(1)
from (select id, cell
      from b_xiecheng_colliding_3710058_20250108100000_5189
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_8742
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_2411
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_3191
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_7647
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_2369
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_5812
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_4378
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250108030000_6741
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_1333
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0) a



select count(1)
from (select cell_sha256_code_list as cell
      from b_xiecheng_colliding_data_loop_cycle
      where (release_time >= "2024-09-30 00:00:00" and release_time <= "2024-09-30 23:59:59")
        and is_delete = 0) cycle
         inner join (select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_6611
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_7063
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240917000000_8962
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240917000000_2970
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_8545
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240917000000_2720
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_3592
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_1763
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_4087
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20240916000000_5690
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and is_delete = 0) score on score.cell = cycle.cell


select count(1)
from (select id, cell
      from b_xiecheng_colliding_3710058_20250108100000_5189
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_8742
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_2411
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_3191
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_7647
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_2369
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250107000000_5812
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_4378
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250108030000_6741
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0
      union all
      select id, cell
      from b_xiecheng_colliding_3710058_20250106000000_1333
      where pd_cell_province != "新疆"
        and pd_cell_province != "西藏"
        and (dataPacket != "830w")
        and is_delete = 0) score
         join (select cell_sha256_code_list as cell
               from b_xiecheng_colliding_data_loop_cycle
               where (release_time >= "2025-01-26 00:00:00" and release_time <= "2025-01-26 23:59:59")
                 and info = "退订用户，不可短信营销"
                 and is_delete = 0) cycle
              on score.cell = cycle.cell



select count(1)
from (select cell_sha256_code_list as cell
      from b_xiecheng_colliding_data_loop_cycle
      where (release_time >= "2025-01-26 00:00:00" and release_time <= "2025-01-26 23:59:59")
        and info = "退订用户，不可短信营销"
        and is_delete = 0) cycle
         inner join (select id, cell
                     from b_xiecheng_colliding_3710058_20250108100000_5189
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_8742
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_2411
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_3191
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_7647
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_2369
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250107000000_5812
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_4378
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250108030000_6741
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0
                     union all
                     select id, cell
                     from b_xiecheng_colliding_3710058_20250106000000_1333
                     where pd_cell_province != "新疆"
                       and pd_cell_province != "西藏"
                       and (dataPacket != "830w")
                       and is_delete = 0) score on score.cell = cycle.cell