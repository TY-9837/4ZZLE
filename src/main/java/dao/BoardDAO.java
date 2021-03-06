package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dto.BoardDTO;

public class BoardDAO {
	public BoardDAO() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws Exception {
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String id = "kh";
		String pw = "kh";
		return DriverManager.getConnection(url, id, pw);
	}

	public int insert(BoardDTO dto) throws Exception {
		String sql = "insert into board values(board_seq.nextval, ?,?,?,sysdate,default)";

		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql)) {
			pstat.setString(1, dto.getTitle());
			pstat.setString(2, dto.getContents());
			pstat.setString(3, dto.getWriter());
			int result = pstat.executeUpdate();
			con.commit();
			return result;
		}
	}

	public List<BoardDTO> selectAll() throws Exception {
		String sql = "select * from board order by seq desc";

		try (Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();) {

			List<BoardDTO> list = new ArrayList<>();
			while (rs.next()) {
				BoardDTO dto = new BoardDTO();
				dto.setSeq(rs.getInt("seq"));
				dto.setTitle(rs.getString("title"));
				dto.setContents(rs.getString("contents"));
				dto.setWriter(rs.getString("writer"));
				dto.setWrite_date(rs.getTimestamp("write_date"));
				dto.setView_count(rs.getInt("view_count"));

				list.add(dto);
			}
			return list;
		}
	}

	public BoardDTO selectBySeq(int seq) throws Exception {
		String sql = "select * from board where seq=?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setInt(1, seq);
			try (ResultSet rs = pstat.executeQuery();) {
				rs.next();
				BoardDTO dto = new BoardDTO();
				dto.setSeq(rs.getInt("seq"));
				dto.setTitle(rs.getString("title"));
				dto.setContents(rs.getString("contents"));
				dto.setWriter(rs.getString("writer"));
				dto.setWrite_date(rs.getTimestamp("write_date"));
				dto.setView_count(rs.getInt("view_count"));
				return dto;
			}
		}
	}

	public int deleteBySeq(int seq) throws Exception {
		String sql = "delete from board where seq =?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setInt(1, seq);
			int result = pstat.executeUpdate();
			return result;
		}
	}

	public int updateBySeq(int seq, String title, String contents) throws Exception {
		String sql = "update board set title=?, contents=? where seq=?";
		try (Connection con = this.getConnection(); PreparedStatement pstat = con.prepareStatement(sql);) {
			pstat.setString(1, title);
			pstat.setString(2, contents);
			pstat.setInt(3, seq);
			int result = pstat.executeUpdate();
			con.commit();
			return result;
		}
	}

	// ?????????
		public int updateViewCount(int seq) throws Exception{
			String sql = "update board set view_count=view_count+1 where seq=?";

			try(Connection con = this.getConnection();
					PreparedStatement pstat = con.prepareStatement(sql);){
				pstat.setInt(1, seq);

				int result = pstat.executeUpdate();
				con.commit();
				return result;
			}
		}

	public int getRecordTotalCount() throws Exception {
		String sql = "select count(*) from board";
		try (Connection con = this.getConnection();
				PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs = pstat.executeQuery();) {
			rs.next();
			return rs.getInt(1);
		}

	}

	public String getPageNavi(int currentPage) throws Exception {

		int recordTotalCount = this.getRecordTotalCount(); //144; // ??? ???????????? ?????? -> ?????? ?????? ????????????????????? ????????? ???????????? ???.

		int recordCountPerPage = 10; // ??? ???????????? ????????? ???????????? ?????? ??? ??????
		int naviCountPerPage = 10; // ??? ???????????? ????????? ????????? ?????? ??? ??????

		int pageTotalCount = 0; // ??? ????????? ???????????? ?????????????

		if (recordTotalCount % recordCountPerPage > 0) { // ?????? ????????? + 1 ?????? ???.
			pageTotalCount = recordTotalCount / recordCountPerPage + 1;
		} else {
			pageTotalCount = recordTotalCount / recordCountPerPage;
		}

		if (currentPage < 1) {
			currentPage = 1;
		} else if (currentPage > pageTotalCount) {
			currentPage = pageTotalCount;
		}

		int startNavi = (currentPage - 1) / naviCountPerPage * naviCountPerPage + 1;
		int endNavi = startNavi + naviCountPerPage - 1;

		if (endNavi > pageTotalCount) {
			endNavi = pageTotalCount;
		}

		boolean needNext = true;
		boolean needPrev = true;

		if (startNavi == 1) {
			needPrev = false;
		}

		if (endNavi == pageTotalCount) {
			needNext = false;
		}

		StringBuilder sb = new StringBuilder(); //??????????????? ????????? ????????? ?????? ????????? ?????????

		if (needPrev) {
			sb.append("<a href='list.board?cpage=" + (startNavi - 1) + "'>?????????</a>");
		}

		for (int i = startNavi; i <= endNavi; i++) {
			if (currentPage == i) {
				sb.append("<a class='current' href='list.board?cpage=" + i + "'>[" + i + "] </a>");
			} else {
				sb.append("<a href='list.board?cpage=" + i + "'>" + i + " </a>");
			}
		}

		if (needNext) {
			sb.append("<a href='list.board?cpage=" + (endNavi + 1) + "'>?????????</a>");
		}
		return sb.toString();
	}
	

	// boradlist?????? ???????????? ????????? ????????? ????????? ?????? ?????????
		public List<BoardDTO> selectByPage(int cpage) throws Exception{

			// ???????????? ????????? ????????????.
			int start = cpage * 10 - 9;
			int end = cpage * 10;

			// ??? ???????????? ???????????? 10?????? ??????????????? ?????? ????????? row_number??? ???????????????, ?????? ????????? ???????????? select ?????????.
			String sql = "select * from (select row_number() over(order by seq desc) line, board.* from board) where line between ? and ?";

			try(Connection con = this.getConnection();
					PreparedStatement pstat = con.prepareStatement(sql);){
				pstat.setInt(1, start);
				pstat.setInt(2, end);

				try(ResultSet rs = pstat.executeQuery();){
					List<BoardDTO> list = new ArrayList<BoardDTO>();

					while(rs.next()) {
						int seq = rs.getInt("seq");
						String title = rs.getString("title");
						String contents = rs.getString("contents");
						String writer = rs.getString("writer");
						Timestamp write_date = rs.getTimestamp("write_date");
						int view_count = rs.getInt("view_count");

						BoardDTO dto = new BoardDTO(seq, title, contents, writer, write_date, view_count);
						list.add(dto);
					}
					return list;
				}
			}
		}

//	public int insertDummy() throws Exception {
//		String sql = "select * from board values(board_seq.nextval, ?, ?, ?, susdate,default)";
//
//		Connection con = this.getConnection();
//
//		for (int i = 0; i < 144; i++) {
//			PreparedStatement pstat = con.prepareStatement(sql);
//			pstat.setString(1, "title : " +i);
//			pstat.setString(2, "contents : " +i);
//			pstat.setString(3, "writer : " +i);
//			pstat.executeUpdate();
//			con.commit();
//		}
//		con.close();
//		return 0 ;
//	}
//
//	
//
//	public static void main(String[] args) throws Exception {
//		BoardDAO dao = new BoardDAO();
//		dao.insertDummy();
//	}

}
