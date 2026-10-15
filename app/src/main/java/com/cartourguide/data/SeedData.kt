package com.cartourguide.data

/**
 * Seed dataset: Southern Vietnam MVP (8 places, 2 regions).
 * Coordinates are approximate WGS-84 from public map data (OSM/Wikipedia) rounded
 * to ~10m; marked NEEDS_CHECK where a field survey is still recommended.
 * Stories are curated summaries; full provenance in sourceUrl. Legends labelled as legends.
 */
object SeedData {
    val places = listOf(
        PlaceEntity("dinh-doc-lap", "Dinh Độc Lập", 10.7770, 106.6953, "sg-central", "di-tich", 5, "st-dinh-doc-lap"),
        PlaceEntity("cho-ben-thanh", "Chợ Bến Thành", 10.7725, 106.6980, "sg-central", "cho", 4, "st-ben-thanh"),
        PlaceEntity("nha-tho-duc-ba", "Nhà thờ Đức Bà Sài Gòn", 10.7798, 106.6990, "sg-central", "ton-giao", 4, "st-duc-ba"),
        PlaceEntity("dia-dao-cu-chi", "Địa đạo Củ Chi (Bến Đình)", 11.1458, 106.4641, "sg-cu-chi", "di-tich", 5, "st-cu-chi"),
        PlaceEntity("chua-vinh-trang", "Chùa Vĩnh Tràng (Mỹ Tho)", 10.3439, 106.3720, "mekong-mytho", "ton-giao", 4, "st-vinh-trang"),
        PlaceEntity("cau-my-thuan", "Cầu Mỹ Thuận", 10.0996, 105.9036, "mekong-mytho", "cau-duong", 3, "st-my-thuan"),
        PlaceEntity("cho-noi-cai-rang", "Chợ nổi Cái Răng (Cần Thơ)", 10.0319, 105.6700, "mekong-cantho", "cho", 4, "st-cai-rang"),
        PlaceEntity("rung-sac-can-gio", "Rừng Sác Cần Giờ", 10.5139, 106.8820, "sg-cangio", "thien-nhien", 4, "st-can-gio"),
    )

    val stories = listOf(
        StoryEntity("st-dinh-doc-lap", "dinh-doc-lap", "Dinh Độc Lập",
            "Mình đang tới gần Dinh Độc Lập nè. Dinh được xây trong thập niên 1960 theo đồ án của kiến trúc sư Ngô Viết Thụ, " +
            "là nơi làm việc của chính quyền Sài Gòn trước năm 1975. Trưa 30 tháng 4 năm 1975, xe tăng tiến vào cổng dinh, " +
            "đánh dấu thời khắc kết thúc chiến tranh. Dinh còn giữ nguyên nhiều phòng họp, phòng khánh tiết và tầng hầm chỉ huy. " +
            "Mình chạy ngang ngắm từ ngoài cũng đẹp, bữa nào rảnh ghé vô tham quan cho biết nha.",
            75, "https://vi.wikipedia.org/wiki/Dinh_%C4%90%E1%BB%99c_L%E1%BA%ADp", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-ben-thanh", "cho-ben-thanh", "Chợ Bến Thành",
            "Phía trước là chợ Bến Thành, biểu tượng quen thuộc của Sài Gòn. Chợ hiện nay được xây đầu thế kỷ 20, " +
            "nổi bật với tháp đồng hồ ở cửa chính. Trong này bán đủ thứ: vải vóc, cà phê, khô, quà lưu niệm. " +
            "Người miền Nam mình hay nói vui, chưa đi chợ Bến Thành là chưa tới Sài Gòn. Mình đi ngang nhớ ngó chừng xe cộ đông lắm nha.",
            60, "https://vi.wikipedia.org/wiki/Ch%E1%BB%A3_B%E1%BA%BFn_Th%C3%A0nh", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-duc-ba", "nha-tho-duc-ba", "Nhà thờ Đức Bà",
            "Kế bên là Nhà thờ Đức Bà, xây cuối thế kỷ 19 thời Pháp, toàn bộ vật liệu chở từ Pháp qua. " +
            "Hai tháp chuông cao hơn 50 mét, gạch đỏ giữ màu tới giờ. Trước sân là tượng Đức Mẹ Hòa Bình. " +
            "Bà con hay ra đây chụp hình, cho bồ câu ăn. Cuối tuần khu này đông vui lắm.",
            60, "https://vi.wikipedia.org/wiki/Nh%C3%A0_th%E1%BB%9D_%C4%90%E1%BB%A9c_B%C3%A0_S%C3%A0i_G%C3%B2n", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-cu-chi", "dia-dao-cu-chi", "Địa đạo Củ Chi",
            "Vùng này là Địa đạo Củ Chi, hệ thống đường hầm chằng chịt mà quân dân Củ Chi đào trong kháng chiến. " +
            "Hầm có nhiều tầng, có bếp, hầm họp, hầm cứu thương. Tương truyền có đoạn người ta chui lọt mà đứng lom khom thôi, " +
            "truyện kể dân gian thì mình nghe cho vui, còn số liệu cụ thể mời bà con coi bảng thuyết minh tại khu di tích nha. " +
            "Đoạn đường này cây cao su nhiều, mình chạy chậm, coi chừng xe ra vô khu tham quan.",
            80, "https://vi.wikipedia.org/wiki/%C4%90%E1%BB%8Ba_%C4%91%E1%BA%A1o_C%E1%BB%A7_Chi", "Wikipedia tiếng Việt", "LEGEND", 1),
        StoryEntity("st-vinh-trang", "chua-vinh-trang", "Chùa Vĩnh Tràng",
            "Sắp tới chùa Vĩnh Tràng ở Mỹ Tho, ngôi chùa lớn có từ thế kỷ 19. Chùa pha trộn kiến trúc Việt, Khmer, Pháp, Trung Hoa, " +
            "nên nhìn vừa quen vừa lạ. Trong chùa có nhiều tượng cổ và ba tượng Phật lớn ngoài sân. " +
            "Bà con đi Tiền Giang hay ghé đây thắp nhang rồi ăn hủ tiếu Mỹ Tho gần đó, ngon hết sảy.",
            65, "https://vi.wikipedia.org/wiki/Ch%C3%B9a_V%C4%A9nh_Tr%C3%A0ng", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-my-thuan", "cau-my-thuan", "Cầu Mỹ Thuận",
            "Mình sắp qua cầu Mỹ Thuận, cây cầu dây văng đầu tiên bắc qua sông Tiền, khánh thành năm 2000. " +
            "Trước khi có cầu, bà con qua phà Mỹ Thuận kẹt dữ lắm, có khi chờ mấy tiếng. Có cầu rồi, từ Sài Gòn về miền Tây khỏe re. " +
            "Qua cầu nhớ giữ làn, gió sông mạnh, mình ngắm sông Tiền rộng mênh mông nè.",
            60, "https://vi.wikipedia.org/wiki/C%E1%BA%A7u_M%E1%BB%B9_Thu%E1%BA%ADn", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-cai-rang", "cho-noi-cai-rang", "Chợ nổi Cái Răng",
            "Gần đây là chợ nổi Cái Răng trên sông Cần Thơ. Hồi xưa ghe xuồng tụ lại mua bán, ai bán gì thì treo lên cây bẹo cho dễ thấy. " +
            "Giờ chợ họp buổi sáng sớm, bán trái cây, ăn sáng, cà phê trên ghe. Muốn đi chợ nổi phải đi sớm, khoảng 5 tới 8 giờ sáng là vui nhất. " +
            "Mình đi trễ thì ghé bến Ninh Kiều ăn lẩu mắm cũng ngon.",
            70, "https://vi.wikipedia.org/wiki/Ch%E1%BB%A3_n%E1%BB%95i_C%C3%A1i_R%C4%83ng", "Wikipedia tiếng Việt", "VERIFIED", 1),
        StoryEntity("st-can-gio", "rung-sac-can-gio", "Rừng Sác Cần Giờ",
            "Khu này là Rừng Sác Cần Giờ, rừng ngập mặn lớn được UNESCO công nhận khu dự trữ sinh quyển. " +
            "Rừng đước, sú, vẹt giữ đất, chắn sóng, là nhà của khỉ, cò, cá sấu hoa cà ở khu bảo tồn. " +
            "Không khí ở đây mát rượi, khác hẳn trong phố. Mình hạ kính xuống nghe mùi rừng, mùi sông, dễ chịu lắm.",
            65, "https://vi.wikipedia.org/wiki/C%E1%BA%A7n_Gi%E1%BB%9D", "Wikipedia tiếng Việt", "VERIFIED", 1),
    )
}
