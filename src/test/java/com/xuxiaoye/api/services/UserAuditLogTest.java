package com.xuxiaoye.api.services;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.google.common.base.CaseFormat;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.services.db.UserAuditDBService;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.db.dto.entity.User;
import com.xuxiaoye.api.services.db.dto.entity.UserAudit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
public class UserAuditLogTest {
    @Autowired
    UserDBService userDBService;

    @Autowired
    UserAuditDBService userAuditDBService;

    EasyRandom easyRandom = new EasyRandom();

    User randomUser() {
        return easyRandom.nextObject(User.class);
    }

    @Nested
    class Create {
        @Test
        void saveOne() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.save(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
        }

        @Test
        void saveOrUpdateOne() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
        }
    }

    @Nested
    class Update {
        @Test
        void saveOrUpdateOne() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            user.setAccountName(updatedValue);

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getAccountName()).isEqualTo(updatedValue);
        }

        @Test
        void updateBatchByIdWithOne() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            user.setAccountName(updatedValue);

            assertDoesNotThrow(() -> userDBService.updateBatchById(List.of(user)));

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getAccountName()).isEqualTo(updatedValue);
        }

        @Test
        void updateBatchByIdWithTwo() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            User user2 = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user2));

            List<UserAudit> audits2 = userAuditDBService.listAuditsByDataPkId(user2.getId());
            assertThat(audits2).hasSize(1);
            assertThat(audits2.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            user.setAccountName(updatedValue);

            // Update
            String updatedValue2 = easyRandom.nextObject(String.class);
            user2.setAccountName(updatedValue2);

            assertDoesNotThrow(() -> userDBService.updateBatchById(List.of(user, user2)));

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getAccountName()).isEqualTo(updatedValue);

            audits2 = userAuditDBService.listAuditsByDataPkId(user2.getId());
            assertThat(audits2).hasSize(2);
            assertThat(audits2.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits2.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits2.get(1).getAccountName()).isEqualTo(updatedValue2);
        }

        @Test
        void updateByLambdaUpdateWrapperAndNoPKChange() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> userDBService.update(
                    new LambdaUpdateWrapper<User>()
                            .eq(User::getAccountName, user.getAccountName())
                            .set(User::getAccountName, updatedValue))
            );

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getAccountName()).isEqualTo(updatedValue);
        }

        @Test
        void updateByUpdateWrapperAndNoPKChange() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, PropertyNamer.methodToProperty(LambdaUtils.extract(User::getAccountName).getImplMethodName()));
            assertDoesNotThrow(() -> userDBService.update(
                    new UpdateWrapper<User>()
                            .eq(column, user.getAccountName())
                            .set(column, updatedValue))
            );

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getAccountName()).isEqualTo(updatedValue);
        }

        @Test
        void updateByLambdaUpdateWrapperAndPKChange() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String newId = easyRandom.nextObject(String.class);
            String newName = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> userDBService.update(
                            new LambdaUpdateWrapper<User>()
                                    .eq(User::getId, user.getId())
                                    .set(User::getId, newId)
                                    .set(User::getAccountName, newName)
                    )
            );

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());

            audits = userAuditDBService.listAuditsByDataPkId(newId);
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(0).getId()).isEqualTo(newId);
        }

        @Test
        void updateByUpdateWrapperAndPKChange() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String newId = easyRandom.nextObject(String.class);
            String newName = easyRandom.nextObject(String.class);

            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, PropertyNamer.methodToProperty(LambdaUtils.extract(User::getId).getImplMethodName()));
            String column2 = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, PropertyNamer.methodToProperty(LambdaUtils.extract(User::getAccountName).getImplMethodName()));

            assertDoesNotThrow(() -> userDBService.update(
                            new UpdateWrapper<User>()
                                    .eq(column, user.getId())
                                    .set(column, newId)
                                    .set(column2, newName)
                    )
            );

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
            assertThat(audits.get(1).getId()).isEqualTo(user.getId());

            audits = userAuditDBService.listAuditsByDataPkId(newId);
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(0).getId()).isEqualTo(newId);
        }
    }

    @Nested
    class Delete {
        @Test
        void removeByIdWithOneEntity() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            assertDoesNotThrow(() -> userDBService.removeById(user));

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
        }

        @Test
        void removeByIdWithIdValue() {
            User user = randomUser();

            assertDoesNotThrow(() -> userDBService.saveOrUpdate(user));

            List<UserAudit> audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            assertDoesNotThrow(() -> userDBService.removeById(user.getId()));

            audits = userAuditDBService.listAuditsByDataPkId(user.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
        }
    }
}