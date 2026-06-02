/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hswebframework.ezorm.core.param.Term
 *  org.hswebframework.ezorm.core.param.Term$Type
 */
package org.jetlinks.community.auth.utils;

import java.util.List;
import org.hswebframework.ezorm.core.param.Term;

public class TermParseUtil {
    public static String parseToSql(List<Term> terms) {
        StringBuilder sql = new StringBuilder("");
        for (Term term : terms) {
            if (term.getValue() != null) {
                if (term.getType() == Term.Type.and) {
                    sql.append(" AND ");
                } else if (term.getType() == Term.Type.or) {
                    sql.append(" OR ");
                }
                sql.append(term.getColumn());
                sql.append(" ").append(term.getTermType()).append(" '").append(term.getValue()).append("'");
            }
            if (term.getTerms() == null || term.getTerms().isEmpty()) continue;
            sql.append(" AND ( ");
            sql.append(TermParseUtil.parseToSql(term.getTerms()));
            sql.append(" ) ");
        }
        return sql.toString();
    }
}

